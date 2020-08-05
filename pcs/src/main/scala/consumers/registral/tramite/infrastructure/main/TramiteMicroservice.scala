package consumers.registral.tramite.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import consumers.registral.tramite.infrastructure.dependency_injection.TramiteActor
import consumers.registral.tramite.infrastructure.http.TramiteStateAPI
import consumers.registral.tramite.infrastructure.kafka.TramiteTransaction
import monitoring.Monitoring

import scala.concurrent.ExecutionContext
import api.actor_transaction.ActorTransaction.Implicits._
import kafka.KafkaMessageProcessorRequirements

object TramiteMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(
      monitoring: Monitoring,
      ec: ExecutionContext
  )(implicit system: ActorSystem, kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: TramiteActor = TramiteActor()
    implicit val e: ExecutionContext = ec
    Seq(
      TramiteStateAPI(actor, monitoring).route,
      TramiteTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
