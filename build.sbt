name := """chatapp"""
organization := "pt-lmt"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"


libraryDependencies ++= Seq(
  jdbc,
  filters,
  evolutions,
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test,
  "org.xerial" % "sqlite-jdbc" % "3.8.6"
)


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "pt-lmt.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "pt-lmt.binders._"
