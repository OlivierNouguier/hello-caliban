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

import doobie.hikari.HikariTransactor
import hellocaliban.conf.DbConfig

import cats.effect.Blocker
import zio.interop.catz._

import zio._

import zio.blocking._
object HelloCalibanDB {

  def hikariTransactor(
      config: DbConfig
  ): ZManaged[Blocking, Throwable, HikariTransactor[Task]] =
    for {
      blockingExecutor <- blockingExecutor.toManaged_
      runtime          <- ZIO.runtime[Any].toManaged_
      transactor <- HikariTransactor
        .newHikariTransactor[Task](
          config.driver,
          config.url,
          config.user,
          config.password,
          runtime.platform.executor.asEC,
          Blocker.liftExecutionContext(blockingExecutor.asEC)
        )
        .toManagedZIO
    } yield transactor

  def makeTxLayer(config: DbConfig) =
    ZLayer.fromManaged(hikariTransactor(config))

}
