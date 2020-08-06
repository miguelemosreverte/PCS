package consumers.registral.domicilio_objeto.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.registral.domicilio_objeto.infrastructure.dependency_injection.DomicilioObjetoActor
import consumers.registral.domicilio_objeto.infrastructure.http.DomicilioObjetoStateAPI
import consumers.registral.domicilio_objeto.infrastructure.kafka.{
  DomicilioObjetoNoTributarioTransaction,
  DomicilioObjetoTributarioTransaction
}
import design_principles.microservice.{Microservice, MicroserviceRequirements}
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

object DomicilioObjetoMicroservice extends Microservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val systemTyped = ctx.system
    implicit val system: ActorSystem = ctx.system.toClassic
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements

    implicit val actor: DomicilioObjetoActor = DomicilioObjetoActor()
    Seq(
      DomicilioObjetoStateAPI(actor, monitoring).route,
      DomicilioObjetoTributarioTransaction(actor, monitoring).route,
      DomicilioObjetoNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
