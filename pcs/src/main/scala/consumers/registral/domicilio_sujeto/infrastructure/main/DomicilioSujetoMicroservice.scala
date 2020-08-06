package consumers.registral.domicilio_sujeto.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.registral.domicilio_sujeto.infrastructure.dependency_injection.DomicilioSujetoActor
import consumers.registral.domicilio_sujeto.infrastructure.http.DomicilioSujetoStateAPI
import consumers.registral.domicilio_sujeto.infrastructure.kafka.{
  DomicilioSujetoNoTributarioTransaction,
  DomicilioSujetoTributarioTransaction
}
import design_principles.microservice.{Microservice, MicroserviceRequirements}
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

object DomicilioSujetoMicroservice extends Microservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val systemTyped = ctx.system
    implicit val system: ActorSystem = ctx.system.toClassic
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements

    implicit val actor: DomicilioSujetoActor = DomicilioSujetoActor()
    Seq(
      DomicilioSujetoStateAPI(actor, monitoring).route,
      DomicilioSujetoTributarioTransaction(actor, monitoring).route,
      DomicilioSujetoNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
