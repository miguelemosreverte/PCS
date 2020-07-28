package consumers.no_registral.cotitularidad.infrastructure.main

import akka.actor.ActorSystem
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

object CotitularidadMicroservice {

  def route(monitoring: Monitoring)(implicit system: ActorSystem): Route = {
    implicit val actor =
      CotitularidadActor.startWithRequirements(KafkaMessageProcessorRequirements.productionSettings())
    import system.dispatcher
    Seq(
      CotitularidadStateAPI(monitoring).route,
      AddCotitularTransaction(monitoring).routeClassic,
      CotitularPublishSnapshotTransaction(monitoring).routeClassic
    ) reduce (_ ~ _)
  }
}
