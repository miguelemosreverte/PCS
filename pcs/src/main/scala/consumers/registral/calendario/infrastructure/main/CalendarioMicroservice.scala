package consumers.registral.calendario.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import consumers.registral.calendario.infrastructure.dependency_injection.CalendarioActor
import consumers.registral.calendario.infrastructure.http.CalendarioStateAPI
import consumers.registral.calendario.infrastructure.kafka.CalendarioTransaction
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

object CalendarioMicroservice {

  import akka.http.scaladsl.server.Directives._
  def routes(implicit system: ActorSystem, monitoring: Monitoring, ec: ExecutionContext): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem = system.toTyped
    implicit val actor = CalendarioActor()
    Seq(
      CalendarioStateAPI().routes,
      CalendarioTransaction().routes
    ) reduce (_ ~ _)
  }
}
