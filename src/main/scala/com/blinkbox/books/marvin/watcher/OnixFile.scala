package com.blinkbox.books.marvin.watcher

import scala.io.Source
import scala.util.Try
import scala.xml.pull._

class OnixFile(source: Source) {
  val xml = new XMLEventReader(source)

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