package com.blinkbox.books.marvin.watcher

import java.io.IOException
import java.nio.file._

import com.typesafe.scalalogging.StrictLogging

import scala.collection.JavaConverters._
import scala.language.postfixOps

class DirectoryScanner(override val rootDirectory: Path) {
  this: ScanningFunctions =>
}

trait ScanningFunctions extends StrictLogging {
  val rootDirectory: Path
  def scan(fileFound: Path => Unit):Unit  = {
    try {
      scanForFiles(rootDirectory, isRoot = true, fileFound)
    } catch {
      case ex: IOException => logger.error(s"IO exception while scanning ${rootDirectory}", ex)
    }
  }

  def scanForFiles(directory: Path, isRoot: Boolean, fileFound: Path => Unit): Unit
}

trait DefaultScanningFunctions extends ScanningFunctions {
  override def scanForFiles(directory: Path, isRoot: Boolean, fileFound: Path => Unit): Unit = {
    val dirStream = Files.newDirectoryStream(directory)
    try {
      dirStream.asScala.foreach {
        case file if Files.isRegularFile(file) && !isRoot => fileFound(file)
        case dir if Files.isDirectory(dir) => scanForFiles(dir, isRoot = false, fileFound)
        case file => logger.warn("File found in the root directory, please remove!", file)
      }
    } finally {
      dirStream.close()
    }
  }
}