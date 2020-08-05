package consumers.registral.actividad_sujeto.infrastructure.main

import akka.actor.typed.scaladsl.adapter._
import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.no_registral.sujeto.infrastructure.main.MicroserviceRequirements
import consumers.registral.actividad_sujeto.infrastructure.dependency_injection.ActividadSujetoActor
import consumers.registral.actividad_sujeto.infrastructure.http.ActividadSujetoStateAPI
import consumers.registral.actividad_sujeto.infrastructure.kafka.ActividadSujetoTransaction
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

object ActividadSujetoMicroservice {

  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    implicit val system: ActorSystem = m.system
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: ActividadSujetoActor = ActividadSujetoActor()
    Seq(
      ActividadSujetoStateAPI(actor, monitoring).route,
      ActividadSujetoTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }

}
