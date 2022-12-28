import Dependencies._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "hive"
ThisBuild / organizationName := "Hive"

lazy val root = (project in file("."))
  .settings(
    name := "files-service",
    libraryDependencies ++=
      cats ++
      circe ++
      database ++
      logging ++
      httpService ++
      scalaTest,

    assembly / test := {},
    assembly / assemblyMergeStrategy := {
      case PathList("org", "slf4j", "impl", ps @ _*) => MergeStrategy.first
      case "logback.xml"                             => MergeStrategy.last
      case x if x endsWith "module-info.class"       => MergeStrategy.discard
      case PathList("META-INF", "maven", "org.webjars", "swagger-ui", "pom.properties") =>
        MergeStrategy.singleOrError
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    },
    assembly / assemblyOutputPath := target.value / "jarfiles" / s"${name.value}.jar"
  )
