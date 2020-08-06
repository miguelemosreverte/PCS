package consumers.registral.domicilio_objeto.infrastructure.main

import scala.concurrent.ExecutionContext

import akka.actor.{ActorSystem, typed}
import akka.http.scaladsl.server.Route

import consumers.registral.domicilio_objeto.infrastructure.dependency_injection.DomicilioObjetoActor
import consumers.registral.domicilio_objeto.infrastructure.http.DomicilioObjetoStateAPI
import consumers.registral.domicilio_objeto.infrastructure.kafka.{DomicilioObjetoNoTributarioTransaction, DomicilioObjetoTributarioTransaction}
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
import kafka.KafkaMessageProcessorRequirements

object DomicilioObjetoMicroservice extends KafkaConsumerMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: KafkaConsumerMicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val systemTyped: typed.ActorSystem[Nothing] = ctx.system
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
