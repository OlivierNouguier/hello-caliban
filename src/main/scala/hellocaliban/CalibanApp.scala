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

import zio._
import zio.console._
import db.HelloCalibanDB
import hellocaliban.conf.Configuration
import hellocaliban.conf.Config

import zio.clock.Clock

import akka.actor.ActorSystem

import hellocaliban.pugrero.PugRepo
import zio.ExitCode

object CalibanApp extends zio.App {

  type AppEnvironment = Console with Clock

  def loadConfig: RIO[Configuration, Config] = RIO.accessM(_.config.load)

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    Managed
      .make(Task(ActorSystem("CalibanApp")))(sys => Task(sys.terminate()).ignore)
      .use { actorSystem =>
        for {
          conf <- loadConfig.provide(Configuration.Live)
          tx = HelloCalibanDB.makeTxLayer(conf.dbConfig)

          fullRepo = tx >>> PugRepo.live ++ ZEnv.live

          _ <- CalibanServer.build(actorSystem).provideLayer(fullRepo)

        } yield ()
      }
      .exitCode

}
