import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

val appVersion = "0.1.0"
val _scalaVersion = "2.11.8"
val paradisePluginVersion = "3.0.0-M1"
val scalaJsDomVersion = "0.9.0"
val scalaJsJQueryVersion = "0.9.0"
val transcendentVersion = "0.2.3.2"

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.8", "-unchecked",
  "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.8", "-target", "1.8", "-g:vars")

lazy val copyJS = TaskKey[Unit]("copyJS", "Copy JavaScript files to root directory")
copyJS := {
  val inDir = baseDirectory.value / "app-nodejs" / "target" / "scala-2.11"
  val outDir = baseDirectory.value
  val files = Seq("awt-nodejs-fastopt.js", "awt-nodejs-fastopt.js.map") map { p => (inDir / p, outDir / p) }
  IO.copy(files, overwrite = true)
}

lazy val jsCommonSettings = Seq(
  scalaVersion := _scalaVersion,
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  relativeSourceMaps := true,
  persistLauncher := true,
  persistLauncher in Test := false,
  homepage := Some(url("https://github.com/ldaniels528/awt")),
  addCompilerPlugin("org.scalamacros" % "paradise" % paradisePluginVersion cross CrossVersion.full),
  resolvers += Resolver.sonatypeRepo("releases"),
  libraryDependencies ++= Seq(
    "be.doeraene" %%% "scalajs-jquery" % scalaJsJQueryVersion,
    "org.scala-js" %%% "scalajs-dom" % scalaJsDomVersion,
    "org.scala-lang" % "scala-reflect" % _scalaVersion
  ))

lazy val root = (project in file("."))
  .aggregate(angularjs, nodejs)
  .dependsOn(angularjs, nodejs)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "awt",
    organization := "com.microsoft",
    version := appVersion,
    scalaVersion := _scalaVersion,
    compile in Compile <<=
      (compile in Compile) dependsOn (fastOptJS in(angularjs, Compile)),
    ivyScala := ivyScala.value map (_.copy(overrideScalaVersion = true)),
    Seq(packageScalaJSLauncher, fastOptJS, fullOptJS) map { packageJSKey =>
      crossTarget in(angularjs, Compile, packageJSKey) := baseDirectory.value / "public" / "javascripts"
    })

lazy val angularjs = (project in file("app-angularjs"))
  .aggregate(shared)
  .dependsOn(shared)
  .enablePlugins(ScalaJSPlugin)
  .settings(jsCommonSettings: _*)
  .settings(
    name := "awt-angularjs",
    organization := "com.microsoft",
    version := appVersion,
    pipelineStages := Seq(gzip),
    resolvers += Resolver.sonatypeRepo("releases"),
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-common" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-browser-core" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-core" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-animate" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-cookies" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-md5" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-nervgh-fileupload" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-sanitize" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-toaster" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-ui-bootstrap" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-ui-router" % transcendentVersion
    ))

lazy val nodejs = (project in file("app-nodejs"))
  .aggregate(shared)
  .dependsOn(shared)
  .enablePlugins(ScalaJSPlugin)
  .settings(jsCommonSettings: _*)
  .settings(
    name := "awt-nodejs",
    organization := "com.microsoft",
    version := appVersion,
    pipelineStages := Seq(gzip),
    resolvers += Resolver.sonatypeRepo("releases"),
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-npm-mean-bundle-minimal" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-splitargs" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-express-csv" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-md5" % transcendentVersion,
      "com.github.ldaniels528" %%% "scalajs-npm-request" % transcendentVersion
    ))

lazy val shared = (project in file("app-shared"))
  .enablePlugins(ScalaJSPlugin)
  .settings(jsCommonSettings: _*)
  .settings(
    name := "awt-shared",
    organization := "com.microsoft",
    version := appVersion,
    pipelineStages := Seq(gzip),
    resolvers += Resolver.sonatypeRepo("releases"),
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-common" % transcendentVersion
    ))

// add the alias
addCommandAlias("fastOptJSPlus", ";fastOptJS;copyJS")

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project root", _: State)) compose (onLoad in Global).value
