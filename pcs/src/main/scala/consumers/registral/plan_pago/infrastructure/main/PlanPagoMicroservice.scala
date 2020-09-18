package consumers.registral.plan_pago.infrastructure.main

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.plan_pago.domain.PlanPagoState
import consumers.registral.plan_pago.infrastructure.dependency_injection.PlanPagoActor
import consumers.registral.plan_pago.infrastructure.http.PlanPagoStateAPI
import consumers.registral.plan_pago.infrastructure.kafka.PlanPagoNoTributarioTransaction
import consumers.registral.plan_pago.infrastructure.kafka.PlanPagoTributarioTransaction
import design_principles.actor_model.mechanism.tell_supervision.TellSupervisor
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import akka.actor.typed.scaladsl.adapter._

class PlanPagoMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {
  implicit val actor: PlanPagoActor = PlanPagoActor(PlanPagoState())
  val tellSupervisor: ActorRef = TellSupervisor.start(actor.shardActor.toClassic)

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      PlanPagoNoTributarioTransaction(tellSupervisor, monitoring),
      PlanPagoTributarioTransaction(tellSupervisor, monitoring)
    )

  override def route: Route =
    (Seq(
      PlanPagoStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)

}
