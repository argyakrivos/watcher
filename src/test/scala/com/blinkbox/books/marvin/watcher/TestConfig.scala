package com.blinkbox.books.marvin.watcher

import ch.qos.logback.classic.{Level, Logger => ClassicLogger}
import com.blinkbox.books.test.MockitoSyrup
import org.scalatest.{BeforeAndAfterEach, FlatSpecLike}
import org.slf4j.{Logger, LoggerFactory}

trait TestConfig extends MockitoSyrup with FlatSpecLike {

}

trait HiddenLogging extends BeforeAndAfterEach {
  this: TestConfig =>

  override def beforeEach: Unit = {
    LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).asInstanceOf[ClassicLogger].setLevel(Level.OFF)
  }
}