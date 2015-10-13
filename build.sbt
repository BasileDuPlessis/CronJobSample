name := """CronJobSample"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "Maven Central Server" at "http://repo1.maven.org/maven2/"

libraryDependencies += "org.subethamail" % "subethasmtp" % "3.1.7"

libraryDependencies ++= Seq(
  cache,
  ws,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.7.play23",
  "com.github.athieriot" %% "specs2-embedmongo" % "0.7.0",
  "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.0",
  "com.github.tomakehurst" % "wiremock" % "1.50"
)

instrumentSettings
