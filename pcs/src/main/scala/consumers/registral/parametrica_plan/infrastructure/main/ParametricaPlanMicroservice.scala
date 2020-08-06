package consumers.registral.parametrica_plan.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.registral.parametrica_plan.infrastructure.dependency_injection.ParametricaPlanActor
import consumers.registral.parametrica_plan.infrastructure.http.ParametricaPlanStateAPI
import consumers.registral.parametrica_plan.infrastructure.kafka.{
  ParametricaPlanNoTributarioTransaction,
  ParametricaPlanTributarioTransaction
}
import design_principles.microservice.{Microservice, MicroserviceRequirements}
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

object ParametricaPlanMicroservice extends Microservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val systemTyped = ctx.system
    implicit val system: ActorSystem = ctx.system.toClassic
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements

    implicit val actor: ParametricaPlanActor = ParametricaPlanActor()
    Seq(
      ParametricaPlanStateAPI(actor, monitoring).route,
      ParametricaPlanTributarioTransaction(actor, monitoring).route,
      ParametricaPlanNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
