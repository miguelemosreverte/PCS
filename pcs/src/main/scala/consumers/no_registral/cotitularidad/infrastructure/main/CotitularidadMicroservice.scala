package consumers.no_registral.cotitularidad.infrastructure.main

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import consumers.no_registral.cotitularidad.infrastructure.dependency_injection.CotitularidadActor
import consumers.no_registral.cotitularidad.infrastructure.http.CotitularidadStateAPI
import consumers.no_registral.cotitularidad.infrastructure.kafka.{
  AddCotitularTransaction,
  CotitularPublishSnapshotTransaction
}
import kafka.KafkaMessageProcessorRequirements
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

object CotitularidadMicroservice {

  def route(monitoring: Monitoring, ec: ExecutionContext)(implicit system: ActorSystem): Route = {
    implicit val actor: ActorRef =
      CotitularidadActor.startWithRequirements(KafkaMessageProcessorRequirements.productionSettings())
    implicit val e: ExecutionContext = ec
    Seq(
      CotitularidadStateAPI(monitoring).route,
      AddCotitularTransaction(monitoring).routeClassic,
      CotitularPublishSnapshotTransaction(monitoring).routeClassic
    ) reduce (_ ~ _)
  }
}
