/*
 * Copyright 2020 Olivier NOUGUIER
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hellocaliban

import caliban.AkkaHttpAdapter

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.Http
import akka.actor.ActorSystem

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

import pug.GraphQLPug

import akka.http.scaladsl.model.StatusCodes

import zio.ZIO
import org.slf4j.LoggerFactory
import hellocaliban.pugrero.PugRepo
import zio.RIO
import zio.clock.Clock
import zio.console.Console
import zio.UIO
import zio.Promise
import zio.Ref
import scala.util.Failure
import scala.util.Success

//import hellocaliban.friends.PugTransactor

trait CountDownLatch {
  def countDown: UIO[Unit]
  def await: UIO[Unit]
}

object CountDownLatch {
  def make(count: Int): UIO[CountDownLatch] =
    for {
      ready <- Promise.make[Nothing, Unit]
      ref   <- Ref.make(count)
    } yield new CountDownLatch {

      override def countDown: zio.UIO[Unit] =
        ref
          .updateAndGet(_ - 1)
          .flatMap {
            case 0 => ready.succeed(()).unit
            case _ => ZIO.unit
          }

      override def await: zio.UIO[Unit] = ready.await

    }
}

object CalibanServer { //extends App with GenericSchema[Console with Clock] {

  val logger = LoggerFactory.getLogger(getClass())

  implicit val rt = zio.Runtime.unsafeFromLayer(Console.live ++ Clock.live)

  def repo: RIO[PugRepo, PugRepo.Service] = RIO.access(_.get)

  def build(
      implicit system: ActorSystem
  ): ZIO[PugRepo, Throwable, Http.ServerBinding] =
    for {
      latch <- CountDownLatch.make(1)
      e     <- repo
      d     <- makeCalibanServer(e).fork

      _ <- ZIO.fromFuture[Unit] { implicit ec =>
        val p = scala.concurrent.Promise[Unit]

        val _ = sys.addShutdownHook {
          logger.info("Gracefull good bye")
          val d = p.success(())
          if (d.isCompleted)
            logger.info(s"Good bye crual world!")
          system.terminate().onComplete {
            case Failure(exception) =>
              logger.error("Error during actor system shutdown!", exception)
            case Success(_) =>
              logger.info("Actor system terminated.")

          }
        }

        p.future
      } *> latch.countDown

      _ <- latch.await
      s <- d.join

      _ <- ZIO.fromFuture(ec => s.unbind())
    } yield s

  def makeCalibanServer(peristence: PugRepo.Service)(
      implicit system: ActorSystem
  ): ZIO[PugRepo, Throwable, Http.ServerBinding] = {
    implicit val executionContext = system.dispatcher

    logger.info("Start server")

    val route = path("api" / "graphql") {
        AkkaHttpAdapter.makeHttpService(rt.unsafeRun(new GraphQLPug(peristence).interp))
      } ~ path("graphiql") {
        getFromResource("graphiql.html")
      } ~ path("") {
        redirect("graphiql", StatusCodes.TemporaryRedirect)
      }

    ZIO.fromFuture(ec => {
      logger.info("Binding")
      Http().bindAndHandle(route, "localhost", 8888)
    })

  }

}
