package consumers.registral.tramite.infrastructure.main

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.tramite.domain.TramiteState
import consumers.registral.tramite.infrastructure.dependency_injection.TramiteActor
import consumers.registral.tramite.infrastructure.http.TramiteStateAPI
import consumers.registral.tramite.infrastructure.kafka.TramiteTransaction
import design_principles.actor_model.mechanism.tell_supervision.TellSupervisor
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import akka.actor.typed.scaladsl.adapter._
class TramiteMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {
  implicit val actor: TramiteActor = TramiteActor(TramiteState())
  val tellSupervisor: ActorRef = TellSupervisor.start(actor.shardActor.toClassic)

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(TramiteTransaction(tellSupervisor, monitoring))

  override def route: Route =
    (Seq(
      TramiteStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)

}
