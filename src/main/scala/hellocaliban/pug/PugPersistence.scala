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

package hellocaliban.friends

import cats.effect._

import doobie.util.ExecutionContexts
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

import zio.interop.catz._
import doobie.hikari.HikariTransactor
import hellocaliban.conf.DbConfig
import scala.concurrent.ExecutionContext
import zio.Reservation

import zio.ZIO
import zio.Managed
import hellocaliban.pug.Pug
import hellocaliban.pug.PugNotFound
import zio.Task
import java.net.URL

trait Persistence extends Serializable {
  val pugPersistence: Persistence.Service[Any]
}

object Persistence {
  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  implicit val urlGet: Get[URL] = Get[String].map(str => new URL(str))
  implicit val urlPut: Put[URL] = Put[String].contramap(url => url.toExternalForm())

  trait Service[R] {
    def findPug(name: String): zio.IO[PugNotFound, Pug] // GET request
    def addPug(pug: Pug): Task[Pug]                     // POST request
  }

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  def makeTx(config: DbConfig, ce: ExecutionContext, be: ExecutionContext) =
    Managed(
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
        .map {
          case (tx, cleanup) => Reservation(ZIO.succeed(tx), _ => cleanup.orDie)
        }
        .uninterruptible
    )

  trait Live extends Persistence {
    protected val tnx: Transactor[Task]

    @SuppressWarnings(Array("org.wartremover.warts.Any"))
    val pugPersistence = new Service[Any] {
      def findPug(name: String): zio.IO[PugNotFound, Pug] =
        sql"SELECT name, nickames, picture_url, color FROM pug WHERE name = $name"
          .query[Pug]
          .unique
          .transact(tnx)
          .orDie

      def addPug(pug: Pug): Task[Pug] = ???
    }
  }
}
