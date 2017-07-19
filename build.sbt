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
  "com.lihaoyi" %%% "utest" % "0.4.8" % "test"
)

jsDependencies ++= Seq(
  "org.webjars.npm" % "baconjs" % "0.7.88" / "Bacon.js" minified "Bacon.min.js",
  RuntimeDOM % "test"
)

testFrameworks += new TestFramework("utest.runner.Framework")
