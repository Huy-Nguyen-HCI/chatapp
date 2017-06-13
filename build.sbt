name := """chatapp"""
organization := "pt-lmt"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

lazy val akkaVersion = "2.4.11"

libraryDependencies ++= Seq(
  filters,
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test,
  "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
)

// Slick database
libraryDependencies ++= Seq(
  "org.xerial" % "sqlite-jdbc" % "3.8.6",
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0"
)

// front-end libraries
libraryDependencies ++= Seq(
  "org.webjars.npm" % "jquery" % "3.2.1",
  "org.webjars.bower" % "bootstrap" % "3.3.7",
  "org.webjars" % "flot" % "0.8.3"
)

herokuAppName in Compile := "morning-headland-89121"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "pt-lmt.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "pt-lmt.binders._"
