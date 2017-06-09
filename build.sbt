name := """chatapp"""
organization := "pt-lmt"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

lazy val akkaVersion = "2.4.11"

libraryDependencies ++= Seq(
  filters,
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test,
  "org.xerial" % "sqlite-jdbc" % "3.8.6",
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
  "org.webjars" % "flot" % "0.8.3",
  "org.webjars" % "bootstrap" % "3.3.6",
  "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "pt-lmt.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "pt-lmt.binders._"
