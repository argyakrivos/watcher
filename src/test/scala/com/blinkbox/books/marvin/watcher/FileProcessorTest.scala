package com.blinkbox.books.marvin.watcher

import java.nio.file._

import akka.actor.{ActorRef, ActorSystem, Actor}
import akka.testkit.{TestActorRef, TestProbe}
import com.blinkbox.books.json.DefaultFormats
import com.blinkbox.books.schemas.ingestion.file.pending.v2.FilePending
import com.blinkbox.books.schemas.ingestion.file.pending.v2.FilePending.Details
import com.google.common.jimfs.{Configuration, Jimfs}
import org.json4s.JsonInput
import org.json4s.jackson.{Serialization, JsonMethods}
import org.junit.runner.RunWith
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.junit.JUnitRunner
import com.blinkbox.books.messaging.Event

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout

@RunWith(classOf[JUnitRunner])
class FileProcessorTest extends TestConfig with ScalaFutures {
  implicit val system = ActorSystem("marvin-watcher")
  implicit val timeout = Timeout(500.millis)
  implicit val formats = new DefaultFormats {}

  behavior of "The File Processor"

  it must "copy the file to storage, send a message and remove the original" in new TestFixture(new SuccessfulFakeRabbit) {
    fileProcessor.fileFound(filePath)

    // A message has been sent to RabbitMQ
    assert(lastRabbitMessage != None, "No messages were sent to RabbitMQ")
    val newLocation = storageDirectory.resolve(publisher).resolve(fileName)
    assert(Files.isRegularFile(newLocation))
  }

  it must "correctly guess epub file type" in new TestFixture(new SuccessfulFakeRabbit) {
    val epubFileLocal = Paths.get(getClass.getResource("/book.epub").toURI.getPath)
    val epubFile = inboundDirectory.resolve(publisher).resolve("test.epub")
    Files.createDirectories(epubFile.getParent)
    Files.copy(epubFileLocal, epubFile)
    assert(Files.isRegularFile(epubFile))

    fileProcessor.fileFound(epubFile)
    val lastMessage = lastRabbitMessage
    assert(lastRabbitMessage != None, "No messages were sent to RabbitMQ")
    val obj = Serialization.read[FilePending.Details](lastMessage.get.body.asString)
    assert(obj.source.contentType == "application/epub+zip")
  }

  it must "correctly guess jpeg file type" in new TestFixture(new SuccessfulFakeRabbit) {
    val jpgFileLocal = Paths.get(getClass.getResource("/image.jpg").toURI.getPath)
    val jpgFile = inboundDirectory.resolve(publisher).resolve("test.jpg")
    Files.createDirectories(jpgFile.getParent)
    Files.copy(jpgFileLocal, jpgFile)
    assert(Files.isRegularFile(jpgFile))

    fileProcessor.fileFound(jpgFile)
    val lastMessage = lastRabbitMessage
    assert(lastRabbitMessage != None, "No messages were sent to RabbitMQ")
    val obj = Serialization.read[FilePending.Details](lastMessage.get.body.asString)
    assert(obj.source.contentType == "image/jpeg")
  }

  it must "correctly guess png file type" in new TestFixture(new SuccessfulFakeRabbit) {
    val pngFileLocal = Paths.get(getClass.getResource("/image.png").toURI.getPath)
    val pngFile = inboundDirectory.resolve(publisher).resolve("test.png")
    Files.createDirectories(pngFile.getParent)
    Files.copy(pngFileLocal, pngFile)
    assert(Files.isRegularFile(pngFile))

    fileProcessor.fileFound(pngFile)
    val lastMessage = lastRabbitMessage
    assert(lastRabbitMessage != None, "No messages were sent to RabbitMQ")
    val obj = Serialization.read[FilePending.Details](lastMessage.get.body.asString)
    assert(obj.source.contentType == "image/png")
  }

  it must "correctly guess onix 2 file type" in new TestFixture(new SuccessfulFakeRabbit) {
    val onixFileLocal = Paths.get(getClass.getResource("/onix.xml").toURI.getPath)
    val onixFile = inboundDirectory.resolve(publisher).resolve("test.xml")
    Files.createDirectories(onixFile.getParent)
    Files.copy(onixFileLocal, onixFile)
    assert(Files.isRegularFile(onixFile))

    fileProcessor.fileFound(onixFile)
    val lastMessage = lastRabbitMessage
    assert(lastRabbitMessage != None, "No messages were sent to RabbitMQ")
    val obj = Serialization.read[FilePending.Details](lastMessage.get.body.asString)
    assert(obj.source.contentType == "application/onix2+xml")
  }

  class TestFixture(val rabbitProvider: RabbitProvider) {
    val fs = Jimfs.newFileSystem(Configuration.unix())
    val inboundDirectory = fs.getPath("/inbound")
    val processingDirectory = fs.getPath("/processing")
    val storageDirectory = fs.getPath("/storage")
    val errorDirectory = fs.getPath("/error")
    Files.createDirectory(inboundDirectory)
    Files.createDirectory(processingDirectory)
    Files.createDirectory(storageDirectory)
    Files.createDirectory(errorDirectory)

    val publisher = "test-publisher"
    val fileName = "9780111222333.epub"
    val filePath = inboundDirectory.resolve(publisher).resolve(fileName)
    Files.createDirectories(filePath.getParent)
    Files.createFile(filePath)

    val fileProcessor = new FileProcessor(inboundDirectory, processingDirectory, storageDirectory, errorDirectory, rabbitProvider.fakeRabbitPublisher, 500.millis)

    def lastRabbitMessage: Option[Event] = {
      val future = (rabbitProvider.fakeRabbitPublisher ? rabbitProvider.LastMessage).mapTo[Option[Event]]
      Await.result(future, 100.millis)
    }
  }

  trait RabbitProvider {
    val fakeRabbitPublisher: ActorRef
    case object LastMessage
  }

  class SuccessfulFakeRabbit extends RabbitProvider {
    val fakeRabbitPublisher = TestActorRef(new Actor {
      var lastMessage: Option[Event] = None
      def receive = {
        case LastMessage => sender ! lastMessage
        case msg: Event =>
          lastMessage = Some(msg)
          sender ! akka.actor.Status.Success(())
      }
    })
  }

//  object FailingFakeRabbit extends RabbitProvider {
//    val fakeRabbitPublisher = TestActorRef(new Actor {
//      var lastMessage: Option[Any] = None
//      def receive = {
//        case LastMessage => sender ! lastMessage
//        case msg =>
//          lastMessage = Some(msg)
//          sender ! akka.actor.Status.Failure(new Throwable)
//      }
//    })
//  }
}
