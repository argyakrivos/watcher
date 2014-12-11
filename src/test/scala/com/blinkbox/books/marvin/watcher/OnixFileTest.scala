package com.blinkbox.books.marvin.watcher

import java.nio.file.{Path, Files}

import com.google.common.jimfs.{Configuration, Jimfs}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class OnixFileTest extends TestConfig {
  behavior of "The Onix Version Detector"

  it must "correctly identify ONIX 2" in new TestFixture {
    val xml = <ONIXmessage></ONIXmessage>
    val xmlString = Source.fromString(xml.toString)
    val onix = new OnixFile(xmlString)

    assert(onix.version == Some(2))
  }

  it must "correctly identify ONIX 3" in new TestFixture {
    val xml = <ONIXmessage release="3"></ONIXmessage>
    val xmlString = Source.fromString(xml.toString)
    val onix = new OnixFile(xmlString)

    assert(onix.version == Some(3))
  }

  it must "correctly identify non-ONIX xml" in new TestFixture {
    val xml = <html></html>
    val xmlString = Source.fromString(xml.toString)
    val onix = new OnixFile(xmlString)

    assert(onix.version == None)
  }

  trait TestFixture {
  }
}
