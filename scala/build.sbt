ThisBuild / scalaVersion := "2.13.16"
ThisBuild / organization := "com.openchat"
ThisBuild / version := "0.1.0"

lazy val root = (project in file(".")).
  settings(
    name := "openchat-kata-2025",

    // Dependencies
    libraryDependencies ++= Seq(
      // Pekko HTTP + required actor/stream modules
      "org.apache.pekko"          %% "pekko-actor-typed"      % "1.2.0",
      "org.apache.pekko"          %% "pekko-stream"           % "1.2.0",
      "org.apache.pekko"          %% "pekko-http"             % "1.2.0",
      "org.apache.pekko"          %% "pekko-http-spray-json"  % "1.2.0",
      // Database
      "org.tpolecat"              %% "doobie-core"            % "1.0.0-RC10",
      "org.tpolecat"              %% "doobie-hikari"          % "1.0.0-RC10",
      "org.xerial"                 % "sqlite-jdbc"            % "3.50.3.0",
      // Cats-effect for Doobie runtime
      "org.typelevel"             %% "cats-effect"            % "3.5.4",
      // HTTP client for tests (sttp client4 over Java 11 HttpClient)
      "com.softwaremill.sttp.client4" %% "core"                % "4.0.11" % Test,
      // Testing
      "org.scalatest"             %% "scalatest"              % "3.2.19" % Test,
      "org.apache.pekko"          %% "pekko-http-testkit"     % "1.2.0"  % Test,
      "org.apache.pekko"          %% "pekko-testkit"          % "1.2.0"  % Test
    ),

    // Helpful compiler flags
    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked"
    )
  )
