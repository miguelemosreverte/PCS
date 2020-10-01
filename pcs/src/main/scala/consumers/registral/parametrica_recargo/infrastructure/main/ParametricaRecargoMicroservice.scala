package consumers.registral.parametrica_recargo.infrastructure.main

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoState
import consumers.registral.parametrica_recargo.infrastructure.dependency_injection.ParametricaRecargoActor
import consumers.registral.parametrica_recargo.infrastructure.http.ParametricaRecargoStateAPI
import consumers.registral.parametrica_recargo.infrastructure.kafka.ParametricaRecargoNoTributarioTransaction
import consumers.registral.parametrica_recargo.infrastructure.kafka.ParametricaRecargoTributarioTransaction
import design_principles.actor_model.mechanism.tell_supervision.TellSupervisor
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import akka.actor.typed.scaladsl.adapter._

class ParametricaRecargoMicroservice(implicit m: KafkaConsumerMicroserviceRequirements)
    extends KafkaConsumerMicroservice {
  implicit val actor: ParametricaRecargoActor = ParametricaRecargoActor(ParametricaRecargoState())
  val tellSupervisor: ActorRef = TellSupervisor.start(actor.shardActor.toClassic)

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      ParametricaRecargoNoTributarioTransaction(actor.shardActor.toClassic, monitoring),
      ParametricaRecargoTributarioTransaction(actor.shardActor.toClassic, monitoring)
    )

  override def route: Route =
    (Seq(
      ParametricaRecargoStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)

}
