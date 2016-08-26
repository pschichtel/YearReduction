name := "YearReduction"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
    "com.typesafe.play" %% "play-json" % "2.5.6",
    "net.sf.biweekly" % "biweekly" % "0.5.0",
    "org.slf4j" % "slf4j-nop" % "1.7.21"
)

mainClass in assembly := Some("tel.schich.yearreducation.Main")