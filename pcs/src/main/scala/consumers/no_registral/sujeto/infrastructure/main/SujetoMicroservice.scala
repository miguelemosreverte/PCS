package consumers.no_registral.sujeto.infrastructure.main

import scala.concurrent.ExecutionContext

import akka.actor.{typed, ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import consumers.no_registral.sujeto.infrastructure.consumer.{
  SujetoNoTributarioTransaction,
  SujetoTributarioTransaction
}
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers.no_registral.sujeto.infrastructure.http.SujetoStateAPI
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import kafka.KafkaMessageProcessorRequirements

object SujetoMicroservice extends KafkaConsumerMicroservice {
  def route(m: KafkaConsumerMicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val system: ActorSystem = ctx
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    implicit val actor: ActorRef = SujetoActor.startWithRequirements(monitoring) //, Some("sujeto-actor-dispatcher"))

    Seq(
      SujetoStateAPI(actor, monitoring).route,
      SujetoTributarioTransaction(actor, monitoring).route,
      SujetoNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
