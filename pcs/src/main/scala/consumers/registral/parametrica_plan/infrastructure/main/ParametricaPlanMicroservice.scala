package consumers.registral.parametrica_plan.infrastructure.main

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.parametrica_plan.domain.ParametricaPlanState
import consumers.registral.parametrica_plan.infrastructure.dependency_injection.ParametricaPlanActor
import consumers.registral.parametrica_plan.infrastructure.http.ParametricaPlanStateAPI
import consumers.registral.parametrica_plan.infrastructure.kafka.ParametricaPlanNoTributarioTransaction
import consumers.registral.parametrica_plan.infrastructure.kafka.ParametricaPlanTributarioTransaction
import design_principles.actor_model.mechanism.tell_supervision.TellSupervisor
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import akka.actor.typed.scaladsl.adapter._

class ParametricaPlanMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {
  implicit val actor: ParametricaPlanActor = ParametricaPlanActor(ParametricaPlanState())
  val tellSupervisor: ActorRef = TellSupervisor.start(actor.shardActor.toClassic)

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(ParametricaPlanNoTributarioTransaction(tellSupervisor, monitoring),
        ParametricaPlanTributarioTransaction(tellSupervisor, monitoring))

  override def route: Route =
    (Seq(
      ParametricaPlanStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)

}
