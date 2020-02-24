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

import caliban.AkkaHttpAdapter
import caliban.schema.GenericSchema

import zio.console.Console
import zio.clock.Clock

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.Http
import akka.actor.ActorSystem

import zio.DefaultRuntime

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

import caliban.schema.GenericSchema

import zio.clock.Clock
import zio.console.Console

import pug.GraphQLPug

import akka.http.scaladsl.model.StatusCodes
import scala.io.StdIn

object CalibanServer extends App with GenericSchema[Console with Clock] {

  implicit val system           = ActorSystem()
  implicit val executionContext = system.dispatcher
  implicit val defaultRuntime   = new DefaultRuntime {}

  val route = path("api" / "graphql") {
      AkkaHttpAdapter.makeHttpService(GraphQLPug.interp)
    } ~ path("graphiql") {
      getFromResource("graphiql.html")
    } ~ path("") {
      redirect("graphiql", StatusCodes.TemporaryRedirect)
    }

  val binding = Http().bindAndHandle(route, "localhost", 8888)

  println("Hit return to stop.")

  val _ = StdIn.readLine()

  binding.flatMap(_.unbind()).onComplete(_ => system.terminate())

}
