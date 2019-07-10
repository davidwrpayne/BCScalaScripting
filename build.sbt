import Dependencies._

ThisBuild / scalaVersion     := "2.11.0"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val AKKA_VERSION: String = "2.5.23"
val AKKA_HTTP_VERSION: String = "10.1.8"
val JSON_4S_VERSION: String = "3.6.7"

val AkkaHttp = Seq(
  // Must use consistent version of Akka across all its submodules
  "com.typesafe.akka" %% "akka-stream" % AKKA_VERSION,
  "com.typesafe.akka" %% "akka-slf4j" % AKKA_VERSION,
  "com.typesafe.akka" %% "akka-testkit" % AKKA_VERSION % "test",
  "com.typesafe.akka" %% "akka-http-xml" % AKKA_HTTP_VERSION,
  "com.typesafe.akka" %% "akka-http" % AKKA_HTTP_VERSION,
  "com.typesafe.akka" %% "akka-http-spray-json" % AKKA_HTTP_VERSION,
  "com.typesafe.akka" %% "akka-http-testkit" % AKKA_HTTP_VERSION % "test"
)

lazy val dependencies = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    "it.justwrote" %% "scala-faker" % "0.3",
    "net.virtual-void" %% "json-lenses" % "0.6.2",
    "org.json4s" %% "json4s-native" % JSON_4S_VERSION,
    scalaTest % Test
)

val otherResolvers = Seq(
  "justwrote" at "http://repo.justwrote.it/releases/",
  "Typesafe Snapshots" at "http://repo.akka.io/snapshots/",
  "Sonotype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  Resolver.jcenterRepo,
  Resolver.mavenCentral
)

lazy val root = (project in file("."))
  .settings(
    name := "OrderCreator",
    resolvers ++= otherResolvers,
    libraryDependencies ++= dependencies ++ AkkaHttp,
    mainClass in assembly := Some("scripts.Boot")
  )


// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
