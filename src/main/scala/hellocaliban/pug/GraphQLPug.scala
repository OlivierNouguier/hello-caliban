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

import scala.language.postfixOps

import java.net.URL
import zio.IO
import zio.UIO
import caliban.GraphQL.graphQL
import caliban.RootResolver
import caliban.schema.Schema
import caliban.schema.ArgBuilder
import caliban.wrappers.Wrappers._
import caliban.wrappers.ApolloTracing.apolloTracing

import scala.util.Try
import caliban.CalibanError.ExecutionError

import zio.duration._
import caliban.GraphQL
import zio.clock.Clock

case class FindPugArgs(name: String)
case class AddPugArgs(pug: Pug)
case class EditPugPictureArgs(name: String, pictureUrl: URL)

case class Queries(findPug: FindPugArgs => IO[PugNotFound, Pug], randomPugPicture: UIO[String])

case class Mutations(
    addPug: AddPugArgs => UIO[Unit],
    editPugPicture: EditPugPictureArgs => IO[PugNotFound, Unit]
)

object GraphQLPug {

  implicit val urlSchema: Schema[Any, URL] = Schema.stringSchema.contramap(_.toString)
  implicit val urlArgBuilder: ArgBuilder[URL] = ArgBuilder.string.flatMap(
    url => Try(new URL(url)).fold(_ => Left(ExecutionError(s"Invalid URL $url")), Right(_))
  )

  val pugService: PugService = new PugService {
    override def findPug(name: String): IO[PugNotFound, Pug] =
      IO.succeed(
        Pug(
          "Patrick",
          List("Pat"),
          Some(new URL("https://m.media-amazon.com/images/I/81tRAIFb9OL._SS500_.jpg")),
          Color.FAWN
        )
      )
    override def randomPugPicture: UIO[String] =
      UIO.succeed("https://m.media-amazon.com/images/I/81tRAIFb9OL._SS500_.jpg")
    override def addPug(pug: Pug): UIO[Unit] = UIO.unit
    override def editPugPicture(name: String, pictureUrl: URL): IO[PugNotFound, Unit] =
      IO.fail(PugNotFound(name))
  }

  val queries = Queries(args => pugService.findPug(args.name), pugService.randomPugPicture)

  val mutations = Mutations(
    args => pugService.addPug(args.pug),
    args => pugService.editPugPicture(args.name, args.pictureUrl)
  )

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  def makeApi: GraphQL[zio.console.Console with Clock] =
    graphQL(RootResolver(queries, mutations)) @@
    maxFields(200) @@               // query analyzer that limit query fields
    maxDepth(30) @@                 // query analyzer that limit query depth
    timeout(3 seconds) @@           // wrapper that fails slow queries
    printSlowQueries(500 millis) @@ // wrapper that logs slow queries
    apolloTracing

  val interp = makeApi.interpreter
}
