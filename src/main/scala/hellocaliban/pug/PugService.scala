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

import zio._
import doobie.util.meta.Meta
import doobie.postgres.implicits.pgEnumStringOpt
import java.{ util => ju }

trait PugService {
  def randomPugPicture: UIO[String]                                        // GET request
  def editPugPicture(name: String, pictureUrl: URL): IO[PugNotFound, Unit] // PUT request
}

sealed trait Color
object Color {
  case object FAWN  extends Color
  case object BLACK extends Color
  case object OTHER extends Color

  @SuppressWarnings(Array("org.wartremover.warts.Serializable"))
  def fromEnum(color: String): Option[Color] = Option(color) collect {
    case "fawn"  => Color.FAWN
    case "black" => Color.BLACK
    case _       => Color.OTHER
  }

  def toEnum(color: Color) = color match {
    case FAWN  => "fawn"
    case BLACK => "black"
    case OTHER => "other"
  }

  implicit val colorMeta = Meta[Color](pgEnumStringOpt("color", fromEnum, toEnum))

}
case class Pug(
    id: ju.UUID,
    name: String,
    nicknames: List[String],
    pictureUrl: Option[URL],
    color: Color
)
case class PugNotFound(name: String) extends Throwable
