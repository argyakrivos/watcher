lazy val root = (project in file(".")).
  settings(
    name := "watcher",
    organization := "com.blinkbox.books.marvin",
    version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0"),
    scalaVersion := "2.11.4",
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7", "-Xfatal-warnings", "-Xfuture"),
    libraryDependencies ++= {
      val akkaV = "2.3.7"
      val sprayV = "1.3.2"
      Seq(
      ...
      )
    }
  ).
  settings(rpmPrepSettings: _*)