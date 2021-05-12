inThisBuild(
   List(
     scalaVersion := "2.13.5", // 2.11.12, or 2.13.5
//     scalaVersion := "3.0.0-RC3", // 2.11.12, or 2.13.5
    semanticdbEnabled := true, // enable SemanticDB
    semanticdbVersion := scalafixSemanticdb.revision, // use Scalafix compatible version
    scalafixDependencies += "com.timushev" %% "zio-magic-comments" % "0.1.0"

   )
 )
// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `hello-caliban` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(addCompilerPlugin("org.scalameta" % "semanticdb-scalac" % "4.4.15" cross CrossVersion.full))
    .settings(
      Dependencies.`akka-http-circe`,
      Dependencies.doobie,
      Dependencies.zio,
      libraryDependencies ++= library.caliban ++ Seq(
          library.pureConfig,
      //    library.zioMagick,
          library.scalaCheck % Test,
          library.scalaTest  % Test,
          library.logback,
          library.postgresql % Runtime
        )
    )
// .settings(
//   javaOptions += "-agentpath:/Applications/YourKit-Java-Profiler-2019.8.app/Contents/Resources/bin/mac/libyjpagent.dylib=delay=10000"
// )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val caliban    = "0.10.0"
      val scalaCheck = "1.15.4"
      val scalaTest  = "3.2.8"
      val logback    = "1.2.3"
      val postgresql = "42.2.20.jre7"
      val zioMagick  = "0.3.2"
    }
    val caliban =
      Seq("caliban", "caliban-akka-http").map("com.github.ghostdogpr" %% _ % Version.caliban)
    val pureConfig = "com.github.pureconfig" %% "pureconfig"     % "0.15.0"
    val scalaCheck = "org.scalacheck"        %% "scalacheck"     % Version.scalaCheck
    val scalaTest  = "org.scalatest"         %% "scalatest"      % Version.scalaTest
    val logback    = "ch.qos.logback"        % "logback-classic" % Version.logback
    val postgresql = "org.postgresql"        % "postgresql"      % Version.postgresql
    val zioMagick  =  "io.github.kitlangton" %% "zio-magic"      % Version.zioMagick
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
  scalafmtSettings

lazy val commonSettings =
  Seq(
    // scalaVersion from .travis.yml via sbt-travisci
    organization := "io.metabookmarks",
    organizationName := "Olivier NOUGUIER",
    startYear := Some(2020),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalacOptions ++= Seq(
        "-deprecation",
        "-encoding",
        "UTF-8",
        "-explaintypes",
        "-Yrangepos",
        "-feature",
        "-language:higherKinds",
        "-language:existentials",
        "-unchecked",
        "-Xlint:_,-type-parameter-shadow",
        //  "-Xfatal-warnings",
        "-Ywarn-numeric-widen",
        "-Ywarn-unused:patvars,-implicits",
        "-Ywarn-value-discard"
      ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
//    Compile / compile / wartremoverWarnings ++= Warts.unsafe,
    Compile / run / fork := true
  )

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )
