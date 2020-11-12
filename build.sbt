import Dependencies._

autoCompilerPlugins := true

lazy val webIn = (project in file("SourceWebService"))
  .settings(
    commonSettings,
    name := "SourceWebService",
    libraryDependencies ++= http4s ++ monix ++ tapir ++ kafka ++ circe ++ tofuCore ++ scalaTest
  )

lazy val validation = (project in file("ValidationService"))
  .settings(
    commonSettings,
    name := "ValidationService",
    libraryDependencies ++= fs2 ++ fs2Kafka ++ scalaTest
  )

lazy val db = (project in file("dbSinkService"))
  .settings(
    commonSettings,
    name := "dbSinkService",
    libraryDependencies ++= fs2 ++ fs2Kafka ++ doobie ++ scalaTest
  )

lazy val webOut = (project in file("RestAccessService"))
  .settings(
    commonSettings,
    name := "RestAccessService",
    libraryDependencies ++= typedSchema ++ doobie ++ scalaTest
  )

val root = (project in file("."))
  .aggregate(webIn, validation, db, webOut)
