package consumers.registral.etapas_procesales.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.no_registral.sujeto.infrastructure.main.MicroserviceRequirements
import consumers.registral.etapas_procesales.infrastructure.dependency_injection.EtapasProcesalesActor
import consumers.registral.etapas_procesales.infrastructure.http.EtapasProcesalesStateAPI
import consumers.registral.etapas_procesales.infrastructure.kafka.{
  EtapasProcesalesNoTributarioTransaction,
  EtapasProcesalesTributarioTransaction
}
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

object EtapasProcesalesMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    implicit val system: ActorSystem = m.system
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: EtapasProcesalesActor = EtapasProcesalesActor()
    Seq(
      EtapasProcesalesStateAPI(actor, monitoring).route,
      EtapasProcesalesTributarioTransaction(actor, monitoring).route,
      EtapasProcesalesNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
