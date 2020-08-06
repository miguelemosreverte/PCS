package consumers.registral.actividad_sujeto.infrastructure.main

import scala.concurrent.ExecutionContext

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import consumers.registral.actividad_sujeto.infrastructure.dependency_injection.ActividadSujetoActor
import consumers.registral.actividad_sujeto.infrastructure.http.ActividadSujetoStateAPI
import consumers.registral.actividad_sujeto.infrastructure.kafka.ActividadSujetoTransaction
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import kafka.KafkaMessageProcessorRequirements

object ActividadSujetoMicroservice extends KafkaConsumerMicroservice {

  def route(m: KafkaConsumerMicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val systemTyped: typed.ActorSystem[Nothing] = ctx.system
    implicit val system: ActorSystem = ctx.system.toClassic
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    implicit val actor: ActividadSujetoActor = ActividadSujetoActor()
    Seq(
      ActividadSujetoStateAPI(actor, monitoring).route,
      ActividadSujetoTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }

}
