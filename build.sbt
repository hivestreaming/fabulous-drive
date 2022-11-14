import Dependencies._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "hive"
ThisBuild / organizationName := "Hive"

lazy val root = (project in file("."))
  .settings(
    name := "Files Service",
    libraryDependencies ++=
      cats ++
      circe ++
      database ++
      logging ++
      httpService ++
      scalaTest
  )
