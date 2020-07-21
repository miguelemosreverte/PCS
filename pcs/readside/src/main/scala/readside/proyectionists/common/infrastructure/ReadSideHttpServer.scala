package readside.proyectionists.common.infrastructure

import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.{Done, actor => classic}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class ReadSideHttpServer(routes: Route, host: String, port: Int, system: ActorSystem[_]) {
  import akka.actor.typed.scaladsl.adapter._
  implicit val classicSystem: classic.ActorSystem = system.toClassic
  private val shutdown = CoordinatedShutdown(classicSystem)

  import system.executionContext

  def start(): Unit = {
    Http().bindAndHandle(routes, host, port).onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(s"ReadSide online at http://$host:$port/", address.getHostString, address.getPort)

        shutdown.addTask(CoordinatedShutdown.PhaseServiceRequestsDone, "http-graceful-terminate") { () =>
          binding.terminate(10.seconds).map { _ =>
            system.log
              .info(s"ReadSide http://$host:$port/ graceful shutdown completed", address.getHostString, address.getPort)
            Done
          }
        }
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }
}
