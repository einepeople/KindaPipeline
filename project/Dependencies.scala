import sbt._
import sbt.Keys.{scalaVersion, scalacOptions, version}

object Dependencies {
  //move to Object for ScalaSteward?
  private val typedSchemaVersion = "0.12.7"
  private val fs2Version = "2.4.4"
  private val fs2KafkaVersion = "1.1.0"
  private val doobieVersion = "0.9.0"
  private val monixVersion = "3.2.2"
  private val tapirVersion = "0.17.0-M6"
  private val http4sVersion = "0.21.7"
  private val circeVersion = "0.12.3"
  private val tofuVersion = "0.8.0"
  private val kafkaVersion = "2.6.0"
  private val scalaTestVersion = "3.2.2"

  lazy val commonSettings =
    Seq(
      version := "0.1",
      scalaVersion := "2.13.3",
      scalacOptions ++= Seq(
        "-deprecation",
        "-encoding",
        "UTF-8",
        "-language:higherKinds",
        "-language:postfixOps",
        "-feature",
        "-Xfatal-warnings"
      ),
      addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
      addCompilerPlugin(
        "org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full
      )
    )

  val kafka =
    List("org.apache.kafka" %% "kafka").map(_ % kafkaVersion)
  val fs2 =
    List("co.fs2" %% "fs2-core", "co.fs2" %% "fs2-io").map(_ % fs2Version)

  val fs2Kafka = List("com.github.fd4s" %% "fs2-kafka").map(_ % fs2KafkaVersion)

  val tofuCore = List("ru.tinkoff" %% "tofu-core").map(_ % tofuVersion)

  val typedSchema = List(
    "ru.tinkoff" %% "typed-schema-swagger",
    "ru.tinkoff" %% "typed-schema-akka-http"
  ).map(_ % typedSchemaVersion)

  val doobie = List(
    "org.tpolecat" %% "doobie-core",
    "org.tpolecat" %% "doobie-hikari",
    "org.tpolecat" %% "doobie-postgres"
    //"org.tpolecat" %% "doobie-scalatest" % "0.9.0" % "test"
  ).map(_ % doobieVersion)

  val gatling = List("ru.tinkoff" %% "gatling-picatinny" % "0.6.0")

  val monix = List("io.monix" %% "monix").map(_ % monixVersion)

  val scalaTest =
    List("org.scalatest" %% "scalatest")
      .map(_ % scalaTestVersion) ++
      List("org.scalatest" %% "scalatest")
        .map(_ % scalaTestVersion % "test")

  val tapir =
    List(
      "com.softwaremill.sttp.tapir" %% "tapir-core",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"
    ).map(_ % tapirVersion)
  val http4s = List(
    "org.http4s" %% "http4s-dsl",
    "org.http4s" %% "http4s-blaze-server",
    "org.http4s" %% "http4s-blaze-client",
    "org.http4s" %% "http4s-circe"
  ).map(_ % http4sVersion)

  val circe = List(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-literal"
  ).map(_ % circeVersion)
}
