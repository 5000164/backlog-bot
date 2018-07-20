name := "backlog-bot"

version := "0.2.1"

scalaVersion := "2.12.6"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint")

libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value
libraryDependencies += "com.github.gilbertw1" %% "slack-scala-client" % "0.2.3"
libraryDependencies += "com.nulab-inc" % "backlog4j" % "2.2.1"
