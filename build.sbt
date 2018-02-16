name := "backlog-bot"

version := "0.1"

scalaVersion := "2.12.4"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint")

libraryDependencies += "com.github.gilbertw1" %% "slack-scala-client" % "0.2.2"
