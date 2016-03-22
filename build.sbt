name := """hbbTvPlugin"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.10.6"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

javaOptions in Test += "-Dconfig.file=" + Option(System.getProperty("config.file")).getOrElse("conf/application.conf")

libraryDependencies ++= Seq(
  ws
  , "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.akka23-SNAPSHOT"
  , "com.amazonaws" % "aws-java-sdk" % "1.7.8.1"
  , "org.scalatestplus" %% "play" % "1.1.0" % "test"
  , "com.typesafe.akka" %% "akka-testkit" % "2.3.3" % "test"
  , "org.julienrf" %% "play-json-variants" % "2.0"
)