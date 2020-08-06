package consumers.registral.juicio.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.registral.juicio.infrastructure.dependency_injection.JuicioActor
import consumers.registral.juicio.infrastructure.http.JuicioStateAPI
import consumers.registral.juicio.infrastructure.kafka.{JuicioNoTributarioTransaction, JuicioTributarioTransaction}
import design_principles.microservice.{Microservice, MicroserviceRequirements}
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

object JuicioMicroservice extends Microservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val systemTyped = ctx.system
    implicit val system: ActorSystem = ctx.system.toClassic
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements

    implicit val actor: JuicioActor = JuicioActor()
    Seq(
      JuicioStateAPI(actor, monitoring).route,
      JuicioTributarioTransaction(actor, monitoring).route,
      JuicioNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
