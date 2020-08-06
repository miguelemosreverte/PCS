package consumers.registral.subasta.infrastructure.main

import scala.concurrent.ExecutionContext

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route

import consumers.registral.subasta.infrastructure.dependency_injection.SubastaActor
import consumers.registral.subasta.infrastructure.http.SubastaStateAPI
import consumers.registral.subasta.infrastructure.kafka.SubastaTransaction
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import kafka.KafkaMessageProcessorRequirements

object SubastaMicroservice extends KafkaConsumerMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: KafkaConsumerMicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val systemTyped: typed.ActorSystem[Nothing] = ctx.system
    implicit val system: ActorSystem = ctx.system.toClassic
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements

    implicit val actor: SubastaActor = SubastaActor()
    Seq(
      SubastaStateAPI(actor, monitoring).route,
      SubastaTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
