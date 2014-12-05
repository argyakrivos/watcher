package com.blinkbox.books.marvin.watcher

import java.nio.file.Paths
import java.nio.file.Path
import scala.io.Source
import scala.util.Try
import scala.xml.pull._

class OnixVersionDetector(file: Path) {
  val xml = new XMLEventReader(Source.fromFile(file.toFile))

  val version: Option[Int] = xml.collectFirst {
    case el: scala.xml.pull.EvElemStart => el
  }.flatMap { node =>
    if (node.label.toString.toLowerCase == "onixmessage") {
      Some(Try(node.attrs("release").toString.toInt).getOrElse(2))
    } else {
      None
    }
  }
}