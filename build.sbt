import AssemblyKeys._

name := "solar-flare"

version := "0.1"

scalaVersion := "2.10.4"

seq(assemblySettings: _*)

mergeStrategy in assembly := {
  //case PathList("org", "datanucleus", xs @ _*)             => MergeStrategy.discard
  case m if m.toLowerCase.endsWith("manifest.mf")          => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$")      => MergeStrategy.discard
  case "log4j.properties"                                  => MergeStrategy.discard
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case "reference.conf"                                    => MergeStrategy.concat
  case _                                                   => MergeStrategy.first
}

libraryDependencies ++= Seq(
  "org.apache.spark"      %% "spark-core"                % "0.9.1",
  "org.apache.spark"      %% "spark-streaming"           % "0.9.1",
  "org.apache.spark"      %% "spark-streaming-mqtt"      % "0.9.1",
  "org.eclipse.paho"       % "mqtt-client"               % "0.4.0",
  "org.json4s"            %% "json4s-jackson"            % "3.2.9",
  "com.datastax.spark"    %% "spark-cassandra-connector" % "1.0.0"
)

resolvers ++= Seq(
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Eclipse Paho Repo" at "https://repo.eclipse.org/content/repositories/paho-releases/"
)

lazy val publisher = project in file("publisher")
lazy val receiver  = project in file("receiver")
