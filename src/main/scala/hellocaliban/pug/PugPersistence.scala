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

import cats.effect._

import doobie.util.ExecutionContexts
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

import zio.interop.catz._
import hellocaliban.pug.Pug
import hellocaliban.pug.PugNotFound
import zio.Task
import java.net.URL
import zio.UIO

package object pugrero {

  import doobie.hikari.HikariTransactor

  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  implicit val urlGet: Get[URL] = Get[String].map(str => new URL(str))
  implicit val urlPut: Put[URL] = Put[String].contramap(url => url.toExternalForm())

  import zio.ZLayer

  import zio.Has

  type PugRepo = Has[PugRepo.Service]

  object PugRepo {
    trait Service {
      def findPug(name: String): zio.IO[PugNotFound, Pug] // GET request
      def addPug(pug: Pug): UIO[Int]
    }

    val live: ZLayer[Has[(HikariTransactor[Task], Task[Unit])], Nothing, PugRepo] =
      ZLayer.fromFunction { tx =>
        new Service {

          val tnx: Transactor[Task] = tx.get._1

          def findPug(name: String): zio.IO[PugNotFound, Pug] =
            sql"SELECT id, name, nicknames, picture_url, color FROM pug WHERE name = $name"
              .query[Pug]
              .unique
              .transact(tnx)
              .orDie

          def addPug(pug: Pug): UIO[Int] =
            sql"INSERT INTO pug (id, name, nicknames, picture_url, color) VALUES (${pug.id},${pug.name},${pug.nicknames},${pug.pictureUrl}, ,${pug.color})".update.run
              .transact(tnx)
              .orDie

        }
      }

  }

}
