package consumers.registral.plan_pago.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.registral.plan_pago.infrastructure.dependency_injection.PlanPagoActor
import consumers.registral.plan_pago.infrastructure.http.PlanPagoStateAPI
import consumers.registral.plan_pago.infrastructure.kafka.{
  PlanPagoNoTributarioTransaction,
  PlanPagoTributarioTransaction
}
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import design_principles.microservice.{Microservice, MicroserviceRequirements}
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

object PlanPagoMicroservice extends KafkaConsumerMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: KafkaConsumerMicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val systemTyped = ctx.system
    implicit val system: ActorSystem = ctx.system.toClassic
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements

    implicit val actor: PlanPagoActor = PlanPagoActor()
    Seq(
      PlanPagoStateAPI(actor, monitoring).route,
      PlanPagoTributarioTransaction(actor, monitoring).route,
      PlanPagoNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
