name := "cors-buster"
organization := "pl.iterators"
version := "1.0.0"
scalaVersion := "2.11.7"

scalacOptions := Seq("-target:jvm-1.8", "-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.4.2"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-core" % akkaV
  )
}

cancelable in Global := true

Revolver.settings
