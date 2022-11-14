import sbt._

object Dependencies {
  lazy val CirceVersion  = "0.14.3"
  lazy val DoobieVersion = "1.0.0-RC2"
  lazy val Http4sVersion = "0.23.12"
  lazy val TapirVersion  = "1.2.0"

  lazy val scalaTest = Seq(
    "org.scalatest" %% "scalatest" % "3.2.11" % Test
  )

  lazy val cats = Seq(
    "org.typelevel" %% "cats-core"   % "2.8.0",
    "org.typelevel" %% "cats-effect" % "3.3.14"
  )

  lazy val httpService = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-core"               % TapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"      % TapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"  % TapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % TapirVersion,

    "org.http4s" %% "http4s-blaze-server"       % Http4sVersion,
    "org.http4s" %% "http4s-circe"              % Http4sVersion,
    "org.http4s" %% "http4s-dsl"                % Http4sVersion,
    "org.http4s" %% "http4s-prometheus-metrics" % Http4sVersion
  )

  lazy val database = Seq(
    "org.tpolecat" %% "doobie-core"      % DoobieVersion,
    "org.tpolecat" %% "doobie-postgres"  % DoobieVersion,
    "org.tpolecat" %% "doobie-hikari"    % DoobieVersion,
    "org.tpolecat" %% "doobie-scalatest" % DoobieVersion % Test,

    "org.flywaydb" % "flyway-core" % "9.7.0"
  )

  lazy val logging = Seq(
    "ch.qos.logback"             %  "logback-classic" % "1.4.4",
    "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.5"
  )

  lazy val circe = Seq(
    "io.circe" %% "circe-core"    % CirceVersion,
    "io.circe" %% "circe-generic" % CirceVersion,
    "io.circe" %% "circe-parser"  % CirceVersion
  )
}
