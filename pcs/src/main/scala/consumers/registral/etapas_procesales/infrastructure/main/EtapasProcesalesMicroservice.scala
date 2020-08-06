package consumers.registral.etapas_procesales.infrastructure.main

import scala.concurrent.ExecutionContext

import akka.actor.{ActorSystem, typed}
import akka.http.scaladsl.server.Route

import consumers.registral.etapas_procesales.infrastructure.dependency_injection.EtapasProcesalesActor
import consumers.registral.etapas_procesales.infrastructure.http.EtapasProcesalesStateAPI
import consumers.registral.etapas_procesales.infrastructure.kafka.{EtapasProcesalesNoTributarioTransaction, EtapasProcesalesTributarioTransaction}
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
import kafka.KafkaMessageProcessorRequirements

object EtapasProcesalesMicroservice extends KafkaConsumerMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: KafkaConsumerMicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val systemTyped: typed.ActorSystem[Nothing] = ctx.system
    implicit val system: ActorSystem = ctx.system.toClassic
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements

    implicit val actor: EtapasProcesalesActor = EtapasProcesalesActor()
    Seq(
      EtapasProcesalesStateAPI(actor, monitoring).route,
      EtapasProcesalesTributarioTransaction(actor, monitoring).route,
      EtapasProcesalesNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
