package consumers.registral.juicio.infrastructure.main

import scala.concurrent.ExecutionContext

import akka.actor.{ActorSystem, typed}
import akka.http.scaladsl.server.Route

import consumers.registral.juicio.infrastructure.dependency_injection.JuicioActor
import consumers.registral.juicio.infrastructure.http.JuicioStateAPI
import consumers.registral.juicio.infrastructure.kafka.{JuicioNoTributarioTransaction, JuicioTributarioTransaction}
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
import kafka.KafkaMessageProcessorRequirements

object JuicioMicroservice extends KafkaConsumerMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: KafkaConsumerMicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val systemTyped: typed.ActorSystem[Nothing] = ctx.system
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
