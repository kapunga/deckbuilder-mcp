ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.7.1"

lazy val root = (project in file("."))
  .settings(
    name := "deckbuilder-mcp",
    Compile / mainClass := Some("dbmcp.DeckBuilderApp"),
    assembly / assemblyMergeStrategy := {
      case "META-INF/versions/9/module-info.class" => MergeStrategy.discard
      case x => (assembly / assemblyMergeStrategy).value(x)
    },
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.13.0",
      "org.typelevel" %% "cats-effect" % "3.6.1",
      "co.fs2" %% "fs2-core" % "3.12.0",
      "io.circe" %% "circe-core" % "0.14.14",
      "io.circe" %% "circe-parser" % "0.14.14",
      "io.circe" %% "circe-generic" % "0.14.14",
      "org.http4s" %% "http4s-core" % "0.23.30",
      "org.http4s" %% "http4s-dsl" % "0.23.30",
      "org.http4s" %% "http4s-ember-server" % "0.23.30",
      "org.http4s" %% "http4s-ember-client" % "0.23.30",
      "org.http4s" %% "http4s-circe" % "0.23.30",
      "ch.linkyard.mcp" %% "mcp-server" % "0.3.2",
      "ch.linkyard.mcp" %% "jsonrpc2-stdio" % "0.3.2",
      "ch.linkyard.mcp" %% "jsonrpc2" % "0.3.2",
      "org.slf4j" % "slf4j-nop" % "2.0.16"
    )
  )

