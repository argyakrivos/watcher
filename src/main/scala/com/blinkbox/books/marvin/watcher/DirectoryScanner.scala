package com.blinkbox.books.marvin.watcher

import java.nio.file._

import com.typesafe.scalalogging.StrictLogging

import scala.collection.JavaConverters._
import scala.language.postfixOps

class DirectoryScanner(override val rootDirectory: Path) extends StrictLogging {
  this: ScanningFunctions =>
}

trait ScanningFunctions {
  val rootDirectory: Path
  def scan(fileFoundFunction: Path => Unit): Unit
}

trait DefaultScanningFunctions extends ScanningFunctions with StrictLogging {
  def scan(fileFoundFunction: Path => Unit) = scanForFiles(rootDirectory, isRoot = true, fileFoundFunction)

  private def scanForFiles(directory: Path, isRoot: Boolean, fileFoundFunction: Path => Unit): Unit = {
    val rootDirStream = Files.newDirectoryStream(directory).asScala
    rootDirStream.foreach {
      case file if Files.isRegularFile(file) && !isRoot => fileFoundFunction(file)
      case dir if Files.isDirectory(dir) => scanForFiles(dir, isRoot = false, fileFoundFunction)
      case file => logger.warn("File found in the root directory, please remove!", file)
    }
  }
}