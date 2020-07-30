package consumers.registral.calendario.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import consumers.registral.calendario.infrastructure.dependency_injection.CalendarioActor
import consumers.registral.calendario.infrastructure.http.CalendarioStateAPI
import consumers.registral.calendario.infrastructure.kafka.CalendarioTransaction
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

object CalendarioMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(monitoring: Monitoring, ec: ExecutionContext)(implicit system: ActorSystem): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: CalendarioActor = CalendarioActor()
    implicit val e: ExecutionContext = ec
    Seq(
      CalendarioStateAPI(actor, monitoring).route,
      CalendarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
