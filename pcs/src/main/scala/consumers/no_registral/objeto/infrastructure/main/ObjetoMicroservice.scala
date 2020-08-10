package consumers.no_registral.objeto.infrastructure.main

import scala.concurrent.ExecutionContext

import akka.actor.{typed, ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import consumers.no_registral.objeto.infrastructure.consumer.{
  ObjetoExencionTransaction,
  ObjetoNoTributarioTransaction,
  ObjetoTributarioTransaction,
  ObjetoUpdateNovedadTransaction
}
import consumers.no_registral.objeto.infrastructure.event_processor.ObjetoNovedadCotitularidadProjectionHandler
import consumers.no_registral.objeto.infrastructure.http._
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import kafka.KafkaMessageProcessorRequirements
object ObjetoMicroservice extends KafkaConsumerMicroservice {

  def route(m: KafkaConsumerMicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._

    implicit val system: akka.actor.typed.ActorSystem[Nothing] = ctx.toTyped
    implicit val classicSystem: akka.actor.ActorSystem = ctx

    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    implicit val actor: ActorRef = SujetoActor.startWithRequirements(monitoring)

    val feedbackLoop = ObjetoNovedadCotitularidadProjectionHandler(monitoring, system)
    feedbackLoop.run()

    Seq(
      feedbackLoop.route,
      ObjetoStateAPI(actor, monitoring).route,
      ObjetoExencionTransaction(actor, monitoring).route,
      ObjetoNoTributarioTransaction(actor, monitoring).route,
      ObjetoTributarioTransaction(actor, monitoring).route,
      ObjetoUpdateNovedadTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
