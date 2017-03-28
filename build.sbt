name := """playing-reactjs"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(jdbc, cache, ws, evolutions, specs2 % Test)

libraryDependencies ++= Seq(
  //"com.typesafe.play" %% "anorm" 				% "2.4.0",
  "com.typesafe.play" %% "anorm" 				% "2.5.0",
	//"org.webjars" 			%% "webjars-play" % "2.4.0-1",
  "org.webjars" 			%% "webjars-play" % "2.5.0-1",
  "org.webjars" 			%  "bootstrap" 		% "3.1.1-2",
  "org.webjars"     	%  "flat-ui"    	% "bcaf2de95e",
  "org.webjars" 			%  "react" 				% "0.13.3",
  "org.webjars" 			%  "marked" 			% "0.3.2"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

lazy val akkaVersion = "2.4.11"

libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % akkaVersion

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
libraryDependencies += "org.pircbotx" % "pircbotx" % "2.1"
libraryDependencies += "com.lihaoyi" %% "upickle" % "0.4.4"
