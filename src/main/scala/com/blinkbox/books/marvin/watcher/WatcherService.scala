package com.blinkbox.books.marvin.watcher

import com.blinkbox.books.config.Configuration
import com.blinkbox.books.logging.{DiagnosticExecutionContext, Loggers}
import com.typesafe.scalalogging.slf4j.StrictLogging

import scala.util.control.NonFatal

object WatcherService extends App with Configuration with Loggers with StrictLogging {
  try {
    val appConfig = AppConfig(config)
  } catch {
    case NonFatal(ex) =>
      logger.error("Error during initialisation of the service", ex)
      System.exit(1)
  }
}