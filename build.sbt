name := "watcher"

organization := "com.blinkbox.books.marvin"

version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0")

scalaVersion := "2.11.4"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7")

libraryDependencies ++= {
  val akkaV = "2.3.7"
  val sprayV = "1.3.2"
  Seq(
    "io.spray"                  %% "spray-testkit"     % sprayV    % Test,
    "com.typesafe.akka"         %% "akka-slf4j"        % akkaV,
    "com.typesafe.akka"         %% "akka-testkit"      % akkaV     % Test,
    "com.blinkbox.books"        %% "common-scala-test" % "0.3.0"   % Test,
    "com.blinkbox.books.hermes" %% "rabbitmq-ha"       % "7.1.1"
  )
}

rpmPrepSettings