name := """hbbTvPlugin"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  cache
  ,ws
  ,"org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.akka23-SNAPSHOT"
  //,"nl.rhinofly" %% "play-s3" % "5.0.0"
)
