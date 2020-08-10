package consumers.registral.domicilio_sujeto.infrastructure.main

import scala.concurrent.ExecutionContext

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route

import consumers.registral.domicilio_sujeto.infrastructure.dependency_injection.DomicilioSujetoActor
import consumers.registral.domicilio_sujeto.infrastructure.http.DomicilioSujetoStateAPI
import consumers.registral.domicilio_sujeto.infrastructure.kafka.{
  DomicilioSujetoNoTributarioTransaction,
  DomicilioSujetoTributarioTransaction
}
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import kafka.KafkaMessageProcessorRequirements

object DomicilioSujetoMicroservice extends KafkaConsumerMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: KafkaConsumerMicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._

    implicit val system: akka.actor.typed.ActorSystem[Nothing] = ctx.toTyped
    implicit val classicSystem: akka.actor.ActorSystem = ctx

    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements

    implicit val actor: DomicilioSujetoActor = DomicilioSujetoActor()
    Seq(
      DomicilioSujetoStateAPI(actor, monitoring).route,
      DomicilioSujetoTributarioTransaction(actor, monitoring).route,
      DomicilioSujetoNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
