package consumers.registral.parametrica_recargo.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.registral.parametrica_recargo.infrastructure.dependency_injection.ParametricaRecargoActor
import consumers.registral.parametrica_recargo.infrastructure.http.ParametricaRecargoStateAPI
import consumers.registral.parametrica_recargo.infrastructure.kafka.{
  ParametricaRecargoNoTributarioTransaction,
  ParametricaRecargoTributarioTransaction
}
import design_principles.microservice.{Microservice, MicroserviceRequirements}
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

object ParametricaRecargoMicroservice extends Microservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val systemTyped = ctx.system
    implicit val system: ActorSystem = ctx.system.toClassic
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements

    implicit val actor: ParametricaRecargoActor = ParametricaRecargoActor()

    Seq(
      ParametricaRecargoStateAPI(actor, monitoring).route,
      ParametricaRecargoTributarioTransaction(actor, monitoring).route,
      ParametricaRecargoNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
