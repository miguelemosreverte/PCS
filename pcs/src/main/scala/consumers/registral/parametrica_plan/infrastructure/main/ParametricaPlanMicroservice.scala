package consumers.registral.parametrica_plan.infrastructure.main

import scala.concurrent.ExecutionContext

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route

import consumers.registral.parametrica_plan.infrastructure.dependency_injection.ParametricaPlanActor
import consumers.registral.parametrica_plan.infrastructure.http.ParametricaPlanStateAPI
import consumers.registral.parametrica_plan.infrastructure.kafka.{
  ParametricaPlanNoTributarioTransaction,
  ParametricaPlanTributarioTransaction
}
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import kafka.KafkaMessageProcessorRequirements

object ParametricaPlanMicroservice extends KafkaConsumerMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: KafkaConsumerMicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._

    implicit val system: akka.actor.typed.ActorSystem[Nothing] = ctx.toTyped
    implicit val classicSystem: akka.actor.ActorSystem = ctx

    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements

    implicit val actor: ParametricaPlanActor = ParametricaPlanActor()
    Seq(
      ParametricaPlanStateAPI(actor, monitoring).route,
      ParametricaPlanTributarioTransaction(actor, monitoring).route,
      ParametricaPlanNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
