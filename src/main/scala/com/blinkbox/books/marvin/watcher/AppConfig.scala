package com.blinkbox.books.marvin.watcher

import java.net.URL
import java.util.concurrent.TimeUnit

import com.blinkbox.books.config._
import com.blinkbox.books.rabbitmq.RabbitMqConfig
import com.blinkbox.books.rabbitmq.RabbitMqConfirmedPublisher.PublisherConfiguration
import com.blinkbox.books.rabbitmq.RabbitMqConsumer.QueueConfiguration
import com.typesafe.config.Config

import scala.concurrent.duration._

case class AppConfig()

object AppConfig {
  val prefix = "service.watcher"
  def apply(config: Config) = new AppConfig()
}
