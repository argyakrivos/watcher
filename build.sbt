lazy val root = (project in file(".")).
  settings(
    name := "watcher-service",
    RpmPrepKeys.appUser := Some("quill"),
    organization := "com.blinkbox.books.marvin",
    version := scala.util.Try(scala.io.Source.fromFile("VERSION").mkString.trim).getOrElse("0.0.0"),
    scalaVersion := "2.11.4",
    scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-target:jvm-1.7", "-Xfatal-warnings", "-Xfuture"),
    libraryDependencies ++= {
      Seq(
        "com.typesafe.akka"         %% "akka-slf4j"        % "2.3.7",
        "com.blinkbox.books"        %% "common-scala-test" % "0.3.0"   % Test,
        "com.blinkbox.books"        %% "common-lang"       % "0.2.1",
        "com.blinkbox.books.hermes" %% "rabbitmq-ha"       % "8.1.0",
        "com.blinkbox.books.hermes" %% "message-schemas"   % "0.7.3",
        "com.google.jimfs"          %  "jimfs"             % "1.0"     % Test,
        "com.blinkbox.books"        %% "common-json"       % "0.2.5",
        "com.blinkbox.books"        %% "common-config"     % "2.1.0",
        "org.apache.tika"           %  "tika-core"         % "1.6"
      )
    }
  ).
  settings(rpmPrepSettings: _*)