// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `hello-caliban` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= library.caliban ++ Seq(
        library.scalaCheck % Test,
        library.scalaTest  % Test,
      )
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val caliban = "0.5.2"
      val scalaCheck = "1.14.2"
      val scalaTest  = "3.1.1"
    }
    val caliban    = Seq("caliban", "caliban-akka-http").map("com.github.ghostdogpr" %% _ % Version.caliban)
    val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck
    val scalaTest  = "org.scalatest"  %% "scalatest"  % Version.scalaTest
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
    scalaVersion := "2.13.1",
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
    "-Xfatal-warnings",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused:patvars,-implicits",
    "-Ywarn-value-discard"
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
    Compile / compile / wartremoverWarnings ++= Warts.unsafe,
)

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true,
  )
