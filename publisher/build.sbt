import AssemblyKeys._

name := "publisher"

version := "1.0"

scalaVersion := "2.11.2"

seq(assemblySettings: _*)

mergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf")          => MergeStrategy.discard
  case _                                                   => MergeStrategy.first
}

libraryDependencies ++= Seq(
  "org.eclipse.paho"       % "mqtt-client"          % "0.4.0",
  "joda-time"              %  "joda-time"           % "2.3"
)

resolvers ++= Seq(
  "Eclipse Paho Repo" at "https://repo.eclipse.org/content/repositories/paho-releases/"
)
