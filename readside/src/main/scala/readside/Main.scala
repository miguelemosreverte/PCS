package readside

import akka.actor.typed.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory
import readside.proyectionists.common.infrastructure.{Guardian, ReadSideHttpRoutes, ReadSideHttpServer}
import serialization.EventSerializer

object Main extends App {
  private val config: Config = Seq(
    ConfigFactory parseString EventSerializer.eventAdapterConf,
    ConfigFactory parseString EventSerializer.serializationConf,
    ConfigFactory.load()
  ).reduce(_ withFallback _)

  implicit val system: ActorSystem[Nothing] =
    ActorSystem[Nothing](Guardian(), "PersonClassificationServiceReadSide", config)
  private val log = LoggerFactory.getLogger(this.getClass)

  log.info("Running Http Server")
  val httpHost = system.settings.config.getString("http.ip")
  val httpPort = system.settings.config.getInt("http.port")
  val routes = new ReadSideHttpRoutes()(system)
  val server = new ReadSideHttpServer(routes.readSide, httpHost, httpPort, system)
  server.start()
}
