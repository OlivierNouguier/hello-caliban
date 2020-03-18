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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterAll
//import doobie.postgres.free.Embedded
import com.opentable.db.postgres.embedded.EmbeddedPostgres
import doobie.util.transactor.Transactor
import scala.concurrent.ExecutionContext
import cats.effect.IO

abstract class EmbededDatabaseTestSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {
  protected var postgres: EmbeddedPostgres = _
  protected var transactor: Transactor[IO] = _

  implicit val ioContextShift = IO.contextShift(ExecutionContext.global)

  override def beforeAll(): Unit = {
    super.beforeAll()
    postgres = EmbeddedPostgres.builder().start()
  }

  override def afterAll(): Unit = {
    postgres.close()
    super.afterAll()
  }
}
