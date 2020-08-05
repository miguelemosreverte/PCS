package consumers.registral.etapas_procesales.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import consumers.registral.etapas_procesales.infrastructure.dependency_injection.EtapasProcesalesActor
import consumers.registral.etapas_procesales.infrastructure.http.EtapasProcesalesStateAPI
import consumers.registral.etapas_procesales.infrastructure.kafka.{
  EtapasProcesalesNoTributarioTransaction,
  EtapasProcesalesTributarioTransaction
}
import monitoring.Monitoring

import scala.concurrent.ExecutionContext
import api.actor_transaction.ActorTransaction.Implicits._
import kafka.KafkaMessageProcessorRequirements

object EtapasProcesalesMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(
      monitoring: Monitoring,
      ec: ExecutionContext
  )(implicit system: ActorSystem, kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: EtapasProcesalesActor = EtapasProcesalesActor()
    implicit val e: ExecutionContext = ec
    Seq(
      EtapasProcesalesStateAPI(actor, monitoring).route,
      EtapasProcesalesTributarioTransaction(actor, monitoring).route,
      EtapasProcesalesNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
