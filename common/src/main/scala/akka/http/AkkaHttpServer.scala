package akka.http

import akka.Done
import akka.actor.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.cluster.ClusterEvent.MemberUp
import akka.http.scaladsl.Http
import akka.http.scaladsl.server._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object AkkaHttpServer {
  def start(routes: Route, host: String, port: Int)(ctx: ActorContext[MemberUp]): Behavior[Done] = {

    import akka.actor.typed.scaladsl.adapter._
    implicit val system: ActorSystem = ctx.system.toClassic
    implicit val ec: ExecutionContext = system.dispatcher

    Behaviors.setup[Done] { ee =>
      Http()(system)
        .bindAndHandle(routes, host, port)
        .onComplete {
          case Success(bound) =>
            println(
              s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/"
            )
          case Failure(e) =>
            ctx.log.error("Server could not start!")
            e.printStackTrace()
            ee.self ! Done
        }

      Behaviors.receiveMessage[Done] { _ =>
        ctx.log.info("AkkaHttpServer gracefully terminated.")
        Behaviors.stopped
      }

    }
  }

}
