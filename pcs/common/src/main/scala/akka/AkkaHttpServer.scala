package akka

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server._
import akka.util.Timeout
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration._

trait AkkaHttpServer {

  implicit val timeout: Timeout = 60.seconds
  def routes: Route

  val host = "0.0.0.0"
  val port = 8081

  val log = LoggerFactory.getLogger(this.getClass)

}
object AkkaHttpServer {
  def start(routes: Route, host: String = "0.0.0.0", port: Int = 8081)(implicit system: ActorSystem): Future[Unit] = {
    import system.dispatcher
    Http()
      .bindAndHandle(routes, host, port)
      .map { binding =>
        log.info(s"Starting ${this.getClass.getSimpleName} on $host:$port")
      }
      .recover {
        case ex =>
          log.error(s"Models observer could not bind to $host:$port/ ${ex.getMessage}")
      }
  }
  val log = LoggerFactory.getLogger(this.getClass)

}
