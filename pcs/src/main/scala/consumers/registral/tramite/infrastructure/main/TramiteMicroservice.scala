package consumers.registral.tramite.infrastructure.main

import scala.concurrent.ExecutionContext

import akka.actor.{ActorSystem, typed}
import akka.http.scaladsl.server.Route

import consumers.registral.tramite.infrastructure.dependency_injection.TramiteActor
import consumers.registral.tramite.infrastructure.http.TramiteStateAPI
import consumers.registral.tramite.infrastructure.kafka.TramiteTransaction
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
import kafka.KafkaMessageProcessorRequirements

object TramiteMicroservice extends KafkaConsumerMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: KafkaConsumerMicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val systemTyped: typed.ActorSystem[Nothing] = ctx.system
    implicit val system: ActorSystem = ctx.system.toClassic
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    implicit val actor: TramiteActor = TramiteActor()
    Seq(
      TramiteStateAPI(actor, monitoring).route,
      TramiteTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
