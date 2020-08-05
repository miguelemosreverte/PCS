package consumers.no_registral.cotitularidad.infrastructure.main

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.no_registral.cotitularidad.infrastructure.dependency_injection.CotitularidadActor
import consumers.no_registral.cotitularidad.infrastructure.http.CotitularidadStateAPI
import consumers.no_registral.cotitularidad.infrastructure.kafka.{
  AddCotitularTransaction,
  CotitularPublishSnapshotTransaction
}
import consumers.no_registral.sujeto.infrastructure.main.MicroserviceRequirements
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

object CotitularidadMicroservice {

  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    implicit val system: ActorSystem = m.system
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    implicit val actor: ActorRef =
      CotitularidadActor.startWithRequirements(kafkaProcesorRequirements)
    Seq(
      CotitularidadStateAPI(actor, monitoring).route,
      AddCotitularTransaction(actor, monitoring).route,
      CotitularPublishSnapshotTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
