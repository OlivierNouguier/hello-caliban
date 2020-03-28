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

import zio.ZIO
import zio.console._
import zio.RIO
import db.HelloCalibanDB
import hellocaliban.conf.Configuration
import hellocaliban.conf.Config

import zio.blocking.Blocking

import zio.clock.Clock
import zio.console
import zio.Task
import zio.Managed
import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext
import hellocaliban.pugrero.PugRepo

object CalibanApp extends zio.App {

  type AppEnvironment = Console with Clock

  def loadConfig: RIO[Configuration, Config] = RIO.accessM(_.config.load)

  def blockingExecutor: RIO[Blocking, ExecutionContext] = RIO.access(_.get.blockingExecutor.asEC)

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    Managed
      .make(Task(ActorSystem("CalibanApp")))(sys => Task(sys.terminate()).ignore)
      .use { actorSystem =>
        for {
          conf       <- loadConfig.provide(Configuration.Live)
          blockingEC <- blockingExecutor
          tx = HelloCalibanDB.makeTx(conf.dbConfig, blockingEC, platform.executor.asEC)

          fullRepo = tx >>> PugRepo.live

          a <- CalibanServer.build(actorSystem).provideLayer(fullRepo)

        } yield 0
      }
      //.fold(_ => 1, _ => 0)
      .catchAll(e => console.putStrLn(e.toString).as(1))
}
