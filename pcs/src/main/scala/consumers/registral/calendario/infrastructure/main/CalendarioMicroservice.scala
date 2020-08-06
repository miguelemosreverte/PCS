package consumers.registral.calendario.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.registral.calendario.infrastructure.dependency_injection.CalendarioActor
import consumers.registral.calendario.infrastructure.http.CalendarioStateAPI
import consumers.registral.calendario.infrastructure.kafka.CalendarioTransaction
import design_principles.microservice.{Microservice, MicroserviceRequirements}
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext
object CalendarioMicroservice extends Microservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val systemTyped = ctx.system
    implicit val system: ActorSystem = ctx.system.toClassic
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements

    implicit val actor: CalendarioActor = CalendarioActor()
    Seq(
      CalendarioStateAPI(actor, monitoring).route,
      CalendarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
