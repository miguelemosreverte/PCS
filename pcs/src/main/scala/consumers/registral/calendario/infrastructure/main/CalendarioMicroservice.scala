package consumers.registral.calendario.infrastructure.main

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.calendario.domain.CalendarioState
import consumers.registral.calendario.infrastructure.dependency_injection.CalendarioActor
import consumers.registral.calendario.infrastructure.http.CalendarioStateAPI
import consumers.registral.calendario.infrastructure.kafka.CalendarioTransaction
import design_principles.actor_model.mechanism.tell_supervision.TellSupervisor
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import akka.actor.typed.scaladsl.adapter._

class CalendarioMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {
  implicit val actor: CalendarioActor = CalendarioActor(CalendarioState())
  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(CalendarioTransaction(actor.shardActor.toClassic, monitoring))

  override def route: Route =
    (Seq(
      CalendarioStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)

}
