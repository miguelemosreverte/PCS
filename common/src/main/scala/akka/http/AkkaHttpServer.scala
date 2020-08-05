package akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server._
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.Future

object AkkaHttpServer {
  val config = ConfigFactory.load()
  def start(route: Route, host: String = config.getString("http.ip"), port: Int = config.getInt("http.port"))(
      implicit system: ActorSystem
  ): Future[ServerBinding] = {
    import system.dispatcher
    Http()(system)
      .bindAndHandle(route, host, port)
  }
  val log = LoggerFactory.getLogger(this.getClass)

}
