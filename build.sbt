name := "Scala.js Bacon"

scalaVersion := "2.12.2"

enablePlugins(ScalaJSPlugin)

scalacOptions := Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xfuture"
)

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
)

jsDependencies ++= Seq(
  "org.webjars.npm" % "baconjs" % "0.7.88" / "Bacon.js" minified "Bacon.min.js",
  RuntimeDOM % "test"
)

logBuffered in Test := false
