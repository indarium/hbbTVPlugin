name := """hbbTvPlugin"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

javaOptions ++= Seq("-Xmx512M", "-Xmx2048M", "-XX:MaxPermSize=2048M")

libraryDependencies ++= Seq(
  cache
  ,ws
  ,"org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.akka23-SNAPSHOT"
  ,"com.amazonaws" % "aws-java-sdk" % "1.7.8.1"
  ,"com.typesafe.akka" %% "akka-testkit" % "2.3.3" % "test"
)
