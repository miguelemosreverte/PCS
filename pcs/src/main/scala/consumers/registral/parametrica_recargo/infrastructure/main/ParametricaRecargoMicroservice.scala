package consumers.registral.parametrica_recargo.infrastructure.main

import scala.concurrent.ExecutionContext

import akka.actor.{ActorSystem, typed}
import akka.http.scaladsl.server.Route

import consumers.registral.parametrica_recargo.infrastructure.dependency_injection.ParametricaRecargoActor
import consumers.registral.parametrica_recargo.infrastructure.http.ParametricaRecargoStateAPI
import consumers.registral.parametrica_recargo.infrastructure.kafka.{ParametricaRecargoNoTributarioTransaction, ParametricaRecargoTributarioTransaction}
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
import kafka.KafkaMessageProcessorRequirements

object ParametricaRecargoMicroservice extends KafkaConsumerMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: KafkaConsumerMicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val systemTyped: typed.ActorSystem[Nothing] = ctx.system
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
