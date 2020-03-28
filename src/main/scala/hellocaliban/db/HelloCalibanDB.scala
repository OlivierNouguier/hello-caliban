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

package hellocaliban.db

import cats.effect._

import doobie.hikari.HikariTransactor
import hellocaliban.conf.DbConfig
import scala.concurrent.ExecutionContext
import zio.Task
import cats.effect.Blocker
import zio.interop.catz._
import doobie.util.ExecutionContexts
import cats.effect.IO

import doobie._
import zio.ZLayer

object HelloCalibanDB {
  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  def makeTx(config: DbConfig, ce: ExecutionContext, be: ExecutionContext) =
    ZLayer.fromAcquireRelease(
      HikariTransactor
        .newHikariTransactor[Task](
          config.driver,
          config.url,
          config.user,
          config.password,
          ce,
          Blocker.liftExecutionContext(be)
        )
        .allocated
        .orDie
    ) {
      case (_, cleanup) => cleanup.orDie
    }

}
