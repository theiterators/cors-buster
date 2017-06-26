name := "cors-buster"
organization := "pl.iterators"
version := "1.0.1"
scalaVersion := "2.12.2"

scalacOptions := Seq("-target:jvm-1.8", "-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.5.2"
  val akkaHttpV = "10.0.7"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV
  )
}

cancelable in Global := true

Revolver.settings
