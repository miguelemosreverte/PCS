package consumers.registral.declaracion_jurada.infrastructure.main

import scala.concurrent.ExecutionContext

import akka.actor.{ActorSystem, typed}
import akka.http.scaladsl.server.Route

import consumers.registral.declaracion_jurada.infrastructure.dependency_injection.DeclaracionJuradaActor
import consumers.registral.declaracion_jurada.infrastructure.http.DeclaracionJuradaStateAPI
import consumers.registral.declaracion_jurada.infrastructure.kafka.DeclaracionJuradaTransaction
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
import kafka.KafkaMessageProcessorRequirements
object DeclaracionJuradaMicroservice extends KafkaConsumerMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: KafkaConsumerMicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val systemTyped: typed.ActorSystem[Nothing] = ctx.system
    implicit val system: ActorSystem = ctx.system.toClassic
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements

    implicit val actor: DeclaracionJuradaActor = DeclaracionJuradaActor()
    Seq(
      DeclaracionJuradaStateAPI(actor, monitoring).route,
      DeclaracionJuradaTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
