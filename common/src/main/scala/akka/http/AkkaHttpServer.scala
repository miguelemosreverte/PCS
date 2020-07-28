package akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, _}
import akka.http.scaladsl.server._
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import monitoring.{Counter, Histogram, Monitoring}
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsArray, JsObject, JsString}

import scala.concurrent.Future

object AkkaHttpServer {
  def start(route: Route, host: String = "0.0.0.0", port: Int = 8081)(implicit system: ActorSystem): Future[Unit] = {
    import system.dispatcher
    Http()
      .bindAndHandle(route, host, port)
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
