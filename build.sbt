val scalaV = "2.11.8"

lazy val akkaVersion = "2.4.11"

lazy val server = (project in file("server")).settings(
  scalaVersion := scalaV,
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  libraryDependencies ++= Seq(
    "com.vmunier" %% "scalajs-scripts" % "1.0.0",
    jdbc, cache, ws, evolutions, specs2 % Test,

    //"com.typesafe.play" %% "anorm" 				% "2.4.0",
    "com.typesafe.play" %% "anorm" % "2.5.0",
    //"org.webjars" 			%% "webjars-play" % "2.4.0-1",
    "org.webjars" %% "webjars-play" % "2.5.0-1",
    "org.webjars" % "bootstrap" % "3.1.1-2",
    "org.webjars" % "flat-ui" % "bcaf2de95e",
    "org.webjars" % "react" % "0.13.3",
    "org.webjars" % "marked" % "0.3.2"
  ),


  //appDependencies = Seq(
  //  "com.feth" %% "play-authenticate" % "0.8.1"
  //),
  resolvers += "JBoss" at "https://repository.jboss.org/",

  resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",

  routesGenerator := InjectedRoutesGenerator,

  // Play provides two styles of routers, one expects its actions to be injected, the
  // other, legacy style, accesses its actions statically.


  libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
  libraryDependencies += "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
  libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,

  libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  libraryDependencies += "org.pircbotx" % "pircbotx" % "2.1",
  libraryDependencies += "com.lihaoyi" %% "upickle" % "0.4.4",

  // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
  EclipseKeys.preTasks := Seq(compile in Compile)
).enablePlugins(PlayScala).
  dependsOn(sharedJvm)

//val scalaJSReactVersion = "0.10.1"
//val scalaJSReactVersion = "0.11.3"
val scalaJSReactVersion = "1.0.0-RC2"


//val scalaCssVersion = "0.3.1"
//val scalaCssVersion = "0.5.1"
//val scalaCssVersion = "0.5.0"

//val reactJSVersion = "0.14.2"

val scalaCssVersion = "0.5.3-RC1"
val reactJSVersion = "15.4.2"

val jsOutDir = "server/public/javascripts/reactjs"

lazy val client = (project in file("client")).settings(
  scalaVersion := scalaV,
  persistLauncher := true,
  persistLauncher in Test := false,

  name := "scalajs-react-template",

  version := "1.0",

  scalaVersion := "2.11.7",

  // create launcher file ( its search for object extends JSApp , make sure there is only one file)
  persistLauncher := true,

  persistLauncher in Test := false,

  libraryDependencies ++= Seq(
    "com.github.japgolly.scalajs-react" %%% "core" % scalaJSReactVersion,
    "com.github.japgolly.scalajs-react" %%% "extra" % scalaJSReactVersion,
    "com.github.japgolly.scalacss" %%% "core" % scalaCssVersion,
    "com.github.japgolly.scalacss" %%% "ext-react" % scalaCssVersion,
    "com.olvind" %%% "scalajs-react-components" % "0.6.0",
    "com.lihaoyi" %%% "upickle" % "0.4.3"
  ),

  // React itself
  //   (react-with-addons.js can be react.js, react.min.js, react-with-addons.min.js)
  //DOM, which doesn't exist by default in the Rhino runner. To make the DOM available in Rhino
  jsDependencies ++= Seq(

    "org.webjars.bower" % "react" % "15.4.2" / "react-with-addons.js"
      minified "react-with-addons.min.js"
      commonJSName "React",

    "org.webjars.bower" % "react" % "15.4.2" / "react-dom.js"
      minified "react-dom.min.js"
      dependsOn "react-with-addons.js"
      commonJSName "ReactDOM",

    "org.webjars.bower" % "react" % "15.4.2" / "react-dom-server.js"
      minified "react-dom-server.min.js"
      dependsOn "react-dom.js"
      commonJSName "ReactDOMServer"
  ),

  // creates single js resource file for easy integration in html page
  skip in packageJSDependencies := false,

  // copy  javascript files to js folder,that are generated using fastOptJS/fullOptJS

  crossTarget in(Compile, fullOptJS) := file(jsOutDir),

  crossTarget in(Compile, fastOptJS) := file(jsOutDir),

  crossTarget in(Compile, packageJSDependencies) := file(jsOutDir),

  crossTarget in(Compile, packageScalaJSLauncher) := file(jsOutDir),

  crossTarget in(Compile, packageMinifiedJSDependencies) := file(jsOutDir),

  artifactPath in(Compile, fastOptJS) := ((crossTarget in(Compile, fastOptJS)).value /
    ((moduleName in fastOptJS).value + "-opt.js")),

  scalacOptions += "-feature",


  //import com.lihaoyi.workbench.Plugin._,

  //workbenchSettings,

  updateBrowsers := updateBrowsers.triggeredBy(fastOptJS in Compile),

  refreshBrowsers := refreshBrowsers.triggeredBy(fastOptJS in Compile),

  bootSnippet := "scalajsreact.template.ReactApp().main();"

).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(scalaVersion := scalaV).
  jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the server project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value
