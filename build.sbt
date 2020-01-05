// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `hello-caliban` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.caliban,
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
      val caliban = "0.4.1"
      val scalaCheck = "1.14.2"
      val scalaTest  = "3.1.0"
    }
    val caliban    = "com.github.ghostdogpr" %% "caliban-akka-http" % Version.caliban
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
    // scalaVersion := "2.13.1",
    organization := "io.metabookmarks",
    organizationName := "Olivier NOUGUIER",
    startYear := Some(2020),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8",
      "-Ywarn-unused:imports",
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
    Compile / compile / wartremoverWarnings ++= Warts.unsafe,
)

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true,
  )
