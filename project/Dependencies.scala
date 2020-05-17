import sbt._
import Keys._

object Dependencies {

  val Versions = new  {
    val `akka-http-circe` = "1.32.0"
    val doobieVersion = "0.9.0"
    val pgEmbedded    = "0.13.3"
    val zioVersion = "1.0.0-RC19"
  }

  val doobie = Seq(
    libraryDependencies ++= Seq(
        // Start with this one
        "org.tpolecat" %% "doobie-core" % Versions.doobieVersion,
        // And add any of these as neede
        "org.tpolecat"             %% "doobie-h2"        % Versions.doobieVersion, // H2 driver 1.4.197 + type mappings.
        "org.tpolecat"             %% "doobie-hikari"    % Versions.doobieVersion, // HikariCP transactor.
        "org.tpolecat"             %% "doobie-postgres"  % Versions.doobieVersion, // Postgres driver 42.2.5 + type mappings.
        "org.tpolecat"             %% "doobie-scalatest" % Versions.doobieVersion % "test", // ScalaTest support for typechecking statements.
        "com.opentable.components" % "otj-pg-embedded"   % Versions.pgEmbedded
      )
  )

  


  val `akka-http-circe` = Seq{
     libraryDependencies ++= Seq(
       "de.heikoseeberger" %% "akka-http-circe" % Versions.`akka-http-circe` 
     )
  }

  val zio = Seq(
    libraryDependencies ++= Seq(
        "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC11",
        "dev.zio" %% "zio-test"         % Versions.zioVersion % "test",
        "dev.zio" %% "zio-test-sbt"     % Versions.zioVersion % "test"
      ),
    testFrameworks ++= Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
}
