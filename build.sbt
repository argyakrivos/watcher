lazy val root = (project in file(".")).
  settings(
    name := "watcher",
    organization := "com.blinkbox.books.marvin",
    version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0"),
    scalaVersion := "2.11.4",
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7", "-Xfatal-warnings", "-Xfuture"),
    libraryDependencies ++= {
      val akkaV = "2.3.7"
      Seq(
        "com.typesafe.akka"         %% "akka-slf4j"        % akkaV,
        "com.typesafe.akka"         %% "akka-testkit"      % akkaV     % Test,
        "com.blinkbox.books"        %% "common-scala-test" % "0.3.0"   % Test,
        "com.blinkbox.books.hermes" %% "rabbitmq-ha"       % "7.1.1"
      )
    }
  ).
  settings(rpmPrepSettings: _*)