name := "YearReduction"

version := "1.0"

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
    "com.softwaremill.sttp" %% "core" % "1.6.0",
    "com.softwaremill.sttp" %% "async-http-client-backend-future" % "1.6.0",
    "com.typesafe.play" %% "play-json" % "2.10.6",
    "net.sf.biweekly" % "biweekly" % "0.6.3",
    "org.slf4j" % "slf4j-nop" % "1.7.26"
)

mainClass in assembly := Some("tel.schich.yearreducation.Main")