import sbt._
import Keys._

object Dependencies {
  val doobieVersion = "0.8.8"
  val pgEmbedded = "0.13.3"
  val doobie = Seq(
    libraryDependencies ++= Seq(
      // Start with this one
      "org.tpolecat" %% "doobie-core"      % doobieVersion,
      // And add any of these as neede
      "org.tpolecat" %% "doobie-h2"        % doobieVersion,          // H2 driver 1.4.197 + type mappings.
      "org.tpolecat" %% "doobie-hikari"    % doobieVersion,          // HikariCP transactor.
      "org.tpolecat" %% "doobie-postgres"  % doobieVersion,          // Postgres driver 42.2.5 + type mappings.
      "org.tpolecat" %% "doobie-scalatest" % doobieVersion % "test",  // ScalaTest support for typechecking statements.
      "com.opentable.components" % "otj-pg-embedded" % pgEmbedded
    )
  )
  
  val zioVersion = "1.0.0-RC18-2"

  val zio = Seq(
    libraryDependencies ++= Seq(
     "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC11",
     "dev.zio" %% "zio-test"     % zioVersion % "test",
     "dev.zio" %% "zio-test-sbt" % zioVersion % "test"
    ),
    testFrameworks ++= Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
}
