ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.4"

lazy val root = (project in file("."))
  .settings(
    name := "retirement_calculator",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.15" % "test"
  )
