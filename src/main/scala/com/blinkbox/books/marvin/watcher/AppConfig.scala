package com.blinkbox.books.marvin.watcher

import java.net.URL
import java.util.concurrent.TimeUnit

import com.blinkbox.books.config._
import com.blinkbox.books.rabbitmq.RabbitMqConfig
import com.blinkbox.books.rabbitmq.RabbitMqConfirmedPublisher.PublisherConfiguration
import com.blinkbox.books.rabbitmq.RabbitMqConsumer.QueueConfiguration
import com.typesafe.config.Config
import com.blinkbox.books.config.RichConfig

import scala.concurrent.duration._

case class AppConfig(processingDirectory: String, inboundDirectory: String, storageDirectory: String, errorDirectory: String, messaging: MessagingConfig)
case class MessagingConfig(rabbitmq: RabbitMqConfig, retryInterval: FiniteDuration, marvin: PublisherConfiguration)

object AppConfig {
  val prefix = "service.watcher"
  def apply(config: Config) = new AppConfig(
    config.getString(s"$prefix.directories.processing"),
    config.getString(s"$prefix.directories.inbound"),
    config.getString(s"$prefix.directories.storage"),
    config.getString(s"$prefix.directories.error"),
    MessagingConfig(config, s"$prefix.rabbitmq")
  )
}

object MessagingConfig {
  def apply(config: Config, prefix: String) = new MessagingConfig(
    RabbitMqConfig(config.getConfig(AppConfig.prefix)),
    config.getFiniteDuration(s"$prefix.retryInterval"),
    PublisherConfiguration(config.getConfig(s"$prefix.output"))
  )
}