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

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

import pug.GraphQLPug

import akka.http.scaladsl.model.StatusCodes

import zio._
import org.slf4j.LoggerFactory
import hellocaliban.pugrero.PugRepo
import zio.clock.Clock
import zio.console._
import scala.util.Failure
import scala.util.Success

import caliban.interop.circe.AkkaHttpCirceAdapter

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

object CalibanServer extends AkkaHttpCirceAdapter {

  val logger = LoggerFactory.getLogger(getClass())

  implicit val rt = zio.Runtime.unsafeFromLayer(Console.live ++ Clock.live)

  def repo(): RIO[PugRepo, PugRepo.Service] = RIO.access(_.get)

  def shutdowHook(system: ActorSystem): ZIO[Console, Throwable, ExitCode] = ZIO.effectAsync {
    callback =>
      sys.addShutdownHook {
        system
          .terminate()
          .onComplete {
            case Failure(exception) =>
              callback(putStrLnErr("bye") *> ZIO.fail(exception))
            case Success(_) =>
              callback(putStrLnErr("bye") *> ZIO.succeed(ExitCode.success))

          }(system.dispatcher)
      }
  }

  def build(
      implicit system: ActorSystem
  ): ZIO[PugRepo with Console, Throwable, ExitCode] =
    for {
      repo <- repo()
      d    <- makeCalibanServer(repo)
      ec   <- shutdowHook(system)

    } yield ec

  def makeCalibanServer(peristence: PugRepo.Service)(
      implicit system: ActorSystem
  ): ZIO[PugRepo, Throwable, Http.ServerBinding] = {
    implicit val executionContext = system.dispatcher

    logger.info("Start server")

    val route = path("api" / "graphql") {
        adapter.makeHttpService(rt.unsafeRun(new GraphQLPug(peristence).interp))
      } ~ path("graphiql") {
        getFromResource("graphiql.html")
      } ~ path("") {
        redirect("graphiql", StatusCodes.TemporaryRedirect)
      }

    ZIO.fromFuture(ec => {
      logger.info("Binding: 9000")
      Http().newServerAt("localhost", 9000).bind(route)
    })

  }

}
