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

package hellocaliban.pug

import java.net.URL
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import zio.Task

import zio.interop.catz._
import zio._

class DoobiePugService(tx: Transactor[Task]) extends PugService {
  def findPug(name: String): zio.IO[PugNotFound, Pug] = ???

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  def s1(): UIO[Unit] =
    sql"""SELECT 1"""
      .query[Int]
      .unique
      .transact(tx)
      .orDie
      .unit
//    sql"SELECT name, nickname, picture_url from pugs WHERE name = $name"
//      .query[Pug]
//      .unique
  def randomPugPicture: zio.UIO[String]                                        = ???
  def addPug(pug: Pug): zio.UIO[Unit]                                          = ???
  def editPugPicture(name: String, pictureUrl: URL): zio.IO[PugNotFound, Unit] = ???
}
