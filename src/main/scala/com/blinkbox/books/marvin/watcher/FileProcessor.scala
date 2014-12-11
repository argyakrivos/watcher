package com.blinkbox.books.marvin.watcher

import java.net.URI
import java.nio.file._

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.blinkbox.books.messaging.{Event, EventHeader}
import com.blinkbox.books.schemas.ingestion.file.pending.v2.FilePending
import com.typesafe.scalalogging.StrictLogging
import org.apache.tika.Tika
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.util.Try
import scala.util.control.NonFatal

class FileProcessor(inboundDirectory: Path, processingDirectory: Path, storageDirectory: Path, errorDirectory: Path, rabbitPublisher: ActorRef, messageTimeout: FiniteDuration) extends StrictLogging {
  val tika: Tika = new Tika()
  implicit val timeout = Timeout(messageTimeout)

  def fileFound(file: Path): Unit = {
    val publisher = inboundDirectory.relativize(file).subpath(0, 1).toString
    val relativeName = inboundDirectory.resolve(publisher).relativize(file).toString
    logger.info(s"Processing the file ${relativeName} from ${publisher}")

    try {
      processFile(file, publisher, relativeName)
    } catch {
      // TODO: send the filename as a separate Greylog key
      case NonFatal(ex) => logger.error(s"Uncaught error while processing file: ${file.toString}", ex)
    }
  }

  private def processFile(file:Path, publisher: String, relativeName: String): Unit = {
    val processingPath = moveToProcessing(file, publisher, relativeName)
    
    val lastModified = new DateTime(Files.getLastModifiedTime(processingPath).toMillis, DateTimeZone.UTC)
    val mimeType = mimeTypeFor(processingPath)

    try {
      val storagePath = copyToStorage(processingPath, publisher, relativeName)
      val token = createToken(storagePath)

      val fileDetails = FilePending.Details(
        FilePending.FileSource(
          lastModified, token, publisher, relativeName, mimeType, FilePending.SystemDetails(
            "Marvin/watcher", WatcherService.Version
          )
        )
      )

      notifyMarvin(fileDetails) match {
        case scala.util.Success(_) => completeProcessing(processingPath)
        case scala.util.Failure(ex) =>
          Files.delete(storagePath)
          logger.error(s"RabbitMQ notification could not be sent for $file", ex)
      }
    } catch {
      case ex: java.nio.file.FileAlreadyExistsException =>
        val errorPath = errorDirectory.resolve(publisher).resolve(relativeName)
        Files.createDirectories(errorPath.getParent)
        Files.move(processingPath, errorPath, StandardCopyOption.ATOMIC_MOVE)
        logger.error(s"File has already been transferred into storage; probably already found. Moving to error directory.")
    }
  }

  private def mimeTypeFor(file: Path): String = {
    val fileStream = Files.newInputStream(file)
    val contentType = Try(tika.detect(fileStream, file.toString)).getOrElse("")
    fileStream.close()
    if (contentType == "application/xml") {
      val source = Source.fromInputStream(Files.newInputStream(file))
      new OnixFile(source).version match {
        case Some(2) => "application/onix2+xml"
        case Some(3) => "application/onix3+xml"
        case _ => contentType
      }
    } else {
      contentType
    }
  }

  private def moveToProcessing(file: Path, publisher: String, relativeName: String):Path = {
    val processingPath = processingDirectory.resolve(publisher).resolve(relativeName)
    logger.debug(s"Moving ${file} to Processing directory: ${processingPath.toString}")
    Files.createDirectories(processingPath.getParent)
    Files.move(file, processingPath, StandardCopyOption.ATOMIC_MOVE)
    processingPath
  }

  private def copyToStorage(file: Path, publisher: String, relativeName: String):Path = {
    val storagePath = storageDirectory.resolve(publisher).resolve(relativeName)
    Files.createDirectories(storagePath.getParent)
    Files.copy(file, storagePath)
    storagePath
  }

  private def createToken(file: Path): URI = {
    new URI(s"bbbmap:testfile:${file.toString}")
  }

  private def notifyMarvin(fileDetails: FilePending.Details): Try[Unit] = {
    val headers = EventHeader(
      originator = "Marvin/watcher",
      userId = None,
      transactionId = None,
      additional = Map[String, String]("referenced-content-type" -> fileDetails.source.contentType)
    )
    val msg = Event.json[FilePending.Details](headers, fileDetails)
    val publishFuture: Future[Any] = rabbitPublisher ? msg
    Try(Await.result(publishFuture, timeout.duration))
  }

  private def completeProcessing(processingPath: Path): Unit = {
    Files.delete(processingPath)
  }
}
