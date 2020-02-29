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

package hellocaliban.conf

import zio.Task
import pureconfig.generic.auto._

import pureconfig.ConfigSource
import zio.RIO

case class Config(api: ApiConfig, dbConfig: DbConfig)
case class ApiConfig(endpoint: String, port: Int)
case class DbConfig(driver: String, url: String, user: String, password: String)

trait Configuration extends Serializable {
  val config: Configuration.Service[Any]
}

object Configuration {

  trait Service[R] {
    def load: RIO[R, Config]
  }

  object Live extends Live

  trait Live extends Configuration {
    val config: Configuration.Service[Any] = new Service[Any] {
      def load: Task[Config] = Task.effect(ConfigSource.default.loadOrThrow[Config])
    }
  }

}
