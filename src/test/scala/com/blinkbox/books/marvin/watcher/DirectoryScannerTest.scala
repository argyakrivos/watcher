package com.blinkbox.books.marvin.watcher

import java.io.IOException
import java.nio.file.{Files, Path}

import com.google.common.jimfs.{Configuration, Jimfs}
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DirectoryScannerTest extends TestConfig {

  behavior of "The DirectoryScanner when there is a pending file"

  it must "call the given fileFound function" in new RealFSFixture {
    val publisher = "test-publisher"
    val fileName = "9780111222333.epub"
    val filePath = inboundDirectory.resolve(publisher).resolve(fileName)
    Files.createDirectories(filePath.getParent)
    Files.createFile(filePath)

    val fileFound = mock[Path => Unit]
    directoryScanner.scan(fileFound)
    verify(fileFound, times(1)).apply(filePath)
  }

  it must "not process dot files" in new RealFSFixture {
    val publisher = "test-publisher"
    val fileName = ".DS_Store"
    val filePath = inboundDirectory.resolve(publisher).resolve(fileName)
    Files.createDirectories(filePath.getParent)
    Files.createFile(filePath)

    val fileFound = mock[Path => Unit]
    directoryScanner.scan(fileFound)
    verify(fileFound, times(0)).apply(filePath)
  }

  it must "not process files in root folders" in new RealFSFixture {
    val fileName = "a_file_in_the_root.epub"
    val filePath = inboundDirectory.resolve(fileName)
    Files.createDirectories(filePath.getParent)
    Files.createFile(filePath)

    val fileFound = mock[Path => Unit]
    directoryScanner.scan(fileFound)
    verify(fileFound, times(0)).apply(filePath)
  }

  "The DirectoryScanner when there is a filesystem failure" must "not die" in new ErrorFSFixture {
    val fileFound = mock[Path => Unit]
    directoryScanner.scan(fileFound)
    // No exceptions should have bubbled up
  }

  trait ScanningFunctionsThatThrowIOException extends ScanningFunctions {
    override def scanForFiles(directory: Path, isRoot: Boolean, fileFoundFunction: Path => Unit):Unit = {
      throw(new IOException)
    }
  }

  trait RealFSFixture {
    val fs = Jimfs.newFileSystem(Configuration.unix())
    val inboundDirectory = fs.getPath("/inbound")
    Files.createDirectory(inboundDirectory)

    val directoryScanner = new DirectoryScanner(inboundDirectory) with DefaultScanningFunctions
  }

  trait ErrorFSFixture {
    val inboundDirectory = mock[Path]
    val directoryScanner = new DirectoryScanner(inboundDirectory) with ScanningFunctionsThatThrowIOException
  }
}