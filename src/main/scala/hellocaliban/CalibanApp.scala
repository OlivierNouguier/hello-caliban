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
import hellocaliban.conf.Configuration
import hellocaliban.conf.Config

import zio.blocking.Blocking

import hellocaliban.friends.Persistence
import zio.clock.Clock
import zio.console
import zio.Task
import zio.Managed
import akka.actor.ActorSystem

import doobie.util.transactor.Transactor

object CalibanApp extends zio.App {

  type AppEnvironment = Console with Clock with Persistence

  def loadConfig: RIO[Configuration, Config] = RIO.accessM(_.config.load)

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    Managed
      .make(Task(ActorSystem("CalibanApp")))(sys => Task(sys.terminate()).ignore)
      .use(
        actorSystem =>
          for {
            conf        <- loadConfig.provide(Configuration.Live)
            blockingEnv <- ZIO.environment[Blocking]
            blockingEC  <- blockingEnv.blocking.blockingExecutor.map(_.asEC)
            tx = Persistence.makeTx(conf.dbConfig, blockingEC, platform.executor.asEC)

            e <- tx.use { transactor =>
              CalibanServer.build(actorSystem).provideSome[zio.ZEnv] { i =>
                new Console.Live with Clock.Live with Persistence.Live {
                  override val tnx: Transactor[Task] = transactor
                }
              }

            }

          } yield 0
      )
      .catchAll(e => console.putStrLn(e.toString).as(1))

}
