package com.blinkbox.books.marvin.watcher

import java.nio.file._

import akka.actor.{ActorSystem, Props}
import com.blinkbox.books.config.Configuration
import com.blinkbox.books.logging.Loggers
import com.blinkbox.books.rabbitmq.{RabbitMq, RabbitMqConfirmedPublisher}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._

object WatcherService extends App with Configuration with Loggers with StrictLogging {
  val Version = scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0")
  try {
    val appConfig = AppConfig(config)

    implicit val system = ActorSystem("marvin-watcher")
    val reliableConnection = RabbitMq.reliableConnection(appConfig.messaging.rabbitmq)
    val rabbitPublisher = system.actorOf(Props(new RabbitMqConfirmedPublisher(reliableConnection, appConfig.messaging.marvin)), "WatcherPublisher")

    val inboundDirectory = Paths.get(appConfig.inboundDirectory)
    val processingDirectory = Paths.get(appConfig.processingDirectory)
    val storageDirectory = Paths.get(appConfig.storageDirectory)
    val errorDirectory = Paths.get(appConfig.errorDirectory)
    val delay = 15.seconds
    val directoryScanner = new DirectoryScanner(inboundDirectory) with DefaultScanningFunctions
    val fileProcessor = new FileProcessor(
      inboundDirectory, processingDirectory, storageDirectory, errorDirectory, rabbitPublisher, appConfig.messaging.marvin.messageTimeout
    )
    logger.info(s"Started Marvin/watcher v${Version}.")
    while (true) {
      logger.info(s"Scanning ${inboundDirectory}")
      directoryScanner.scan(fileProcessor.fileFound)
      logger.info(s"Waiting ${delay} before starting a new scan.")
      Thread.sleep(delay.toMillis)
    }
  } catch {
    case ex: Throwable =>
      logger.error("Error during execution of the service", ex)
      System.exit(1)
  }
}