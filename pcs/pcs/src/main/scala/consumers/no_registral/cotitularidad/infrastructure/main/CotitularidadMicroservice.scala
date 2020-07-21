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

object CotitularidadMicroservice {

  def routes(implicit system: ActorSystem): Route = {
    implicit val actor =
      CotitularidadActor.startWithRequirements(KafkaMessageProcessorRequirements.productionSettings())
    Seq(
      CotitularidadStateAPI().routes,
      AddCotitularTransaction().routesClassic,
      CotitularPublishSnapshotTransaction().routesClassic
    ) reduce (_ ~ _)
  }
}
