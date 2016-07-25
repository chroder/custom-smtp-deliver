name := "cloudservices"

version := "1.0"

scalaVersion := "2.10.4"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.2",
  "org.slf4j" % "slf4j-api" % "1.7.6",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "net.liftweb" %% "lift-json" % "2.5",
  "org.apache.mina" % "mina-core" % "2.0.7",
  "com.sun.mail" % "javax.mail" % "1.5.1",
  "commons-lang" % "commons-lang" % "2.6",
  "commons-io" % "commons-io" % "2.4"
)

unmanagedBase <<= baseDirectory { base => base / "lib" }