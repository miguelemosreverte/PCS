package consumers.registral.etapas_procesales.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.registral.etapas_procesales.infrastructure.dependency_injection.EtapasProcesalesActor
import consumers.registral.etapas_procesales.infrastructure.http.EtapasProcesalesStateAPI
import consumers.registral.etapas_procesales.infrastructure.kafka.{
  EtapasProcesalesNoTributarioTransaction,
  EtapasProcesalesTributarioTransaction
}
import design_principles.microservice.{Microservice, MicroserviceRequirements}
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

object EtapasProcesalesMicroservice extends Microservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val systemTyped = ctx.system
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
