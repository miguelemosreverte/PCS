package consumers.registral.calendario.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.no_registral.sujeto.infrastructure.main.MicroserviceRequirements
import consumers.registral.calendario.infrastructure.dependency_injection.CalendarioActor
import consumers.registral.calendario.infrastructure.http.CalendarioStateAPI
import consumers.registral.calendario.infrastructure.kafka.CalendarioTransaction
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext
object CalendarioMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    implicit val system: ActorSystem = m.system
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: CalendarioActor = CalendarioActor()
    Seq(
      CalendarioStateAPI(actor, monitoring).route,
      CalendarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
