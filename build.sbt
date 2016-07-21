import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

val appVersion = "0.1.0"
val meanjsVersion = "0.2.1.1"

val _scalaVersion = "2.11.8"
val paradisePluginVersion = "3.0.0-M1"
val scalaJsDomVersion = "0.9.0"
val scalaJsJQueryVersion = "0.9.0"

scalacOptions ++= Seq("-deprecation", "-encoding", "UTF-8", "-feature", "-target:jvm-1.8", "-unchecked",
  "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint")

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.8", "-target", "1.8", "-g:vars")

val scalaJsOutputDir = Def.settingKey[File]("Directory for Javascript files output by ScalaJS")

lazy val root = (project in file("."))
  .aggregate(nodejs)
  .settings(
    name := "awt",
    organization := "com.microsoft",
    version := appVersion,
    scalaVersion := _scalaVersion
  )

val jsCommonSettings = Seq(
  scalaVersion := _scalaVersion,
  scalacOptions ++= Seq("-feature", "-deprecation"),
  scalacOptions in(Compile, doc) ++= Seq(
    "-no-link-warnings" // Suppresses problems with Scaladoc @throws links
  ),
  relativeSourceMaps := true,
  persistLauncher := true,
  persistLauncher in Test := false,
  homepage := Some(url("https://github.com/ldaniels528/awt")),
  addCompilerPlugin("org.scalamacros" % "paradise" % paradisePluginVersion cross CrossVersion.full),
  resolvers += "releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2",
  libraryDependencies ++= Seq(
    "be.doeraene" %%% "scalajs-jquery" % scalaJsJQueryVersion,
    "org.scala-js" %%% "scalajs-dom" % scalaJsDomVersion,
    "org.scala-lang" % "scala-reflect" % _scalaVersion
  )
)

lazy val angularjs = (project in file("app-angularjs"))
  .aggregate(shared)
  .dependsOn(shared)
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .settings(jsCommonSettings: _*)
  .settings(
    name := "awt-angularjs",
    organization := "com.microsoft",
    version := appVersion,
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-angularjs-core" % meanjsVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-animate" % meanjsVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-cookies" % meanjsVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-md5" % meanjsVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-nervgh-fileupload" % meanjsVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-sanitize" % meanjsVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-toaster" % meanjsVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-ui-bootstrap" % meanjsVersion,
      "com.github.ldaniels528" %%% "scalajs-angularjs-ui-router" % meanjsVersion
    )
  )

lazy val nodejs = (project in file("app-nodejs"))
  .aggregate(angularjs)
  .dependsOn(angularjs)
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .settings(jsCommonSettings: _*)
  .settings(
    name := "awt-nodejs",
    organization := "com.microsoft",
    version := appVersion,
    pipelineStages := Seq(gzip, scalaJSProd),
    relativeSourceMaps := true,
    Seq(packageScalaJSLauncher, fastOptJS, fullOptJS) map { packageJSKey =>
      crossTarget in(angularjs, Compile, packageJSKey) := baseDirectory.value / "public" / "javascripts"
    },
    compile in Compile <<=
      (compile in Compile) dependsOn (fastOptJS in(angularjs, Compile)),
    ivyScala := ivyScala.value map (_.copy(overrideScalaVersion = true)),
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-nodejs-mean-bundle-minimal" % meanjsVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-bcrypt" % meanjsVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-elgs-splitargs" % meanjsVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-express-csv" % meanjsVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-pvorb-md5" % meanjsVersion,
      "com.github.ldaniels528" %%% "scalajs-nodejs-request" % meanjsVersion
    )
  )

lazy val shared = (project in file("app-shared"))
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .settings(jsCommonSettings: _*)
  .settings(
    name := "awt-commonjs",
    organization := "com.microsoft",
    version := appVersion,
    libraryDependencies ++= Seq(
      "com.github.ldaniels528" %%% "scalajs-common" % meanjsVersion
    )
  )

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project root", _: State)) compose (onLoad in Global).value
