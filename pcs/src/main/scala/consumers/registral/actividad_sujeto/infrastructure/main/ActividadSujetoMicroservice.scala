package consumers.registral.actividad_sujeto.infrastructure.main

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.actividad_sujeto.domain.ActividadSujetoState
import consumers.registral.actividad_sujeto.infrastructure.dependency_injection.ActividadSujetoActor
import consumers.registral.actividad_sujeto.infrastructure.http.ActividadSujetoStateAPI
import consumers.registral.actividad_sujeto.infrastructure.kafka.ActividadSujetoTransaction
import design_principles.actor_model.mechanism.tell_supervision.TellSupervisor
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import akka.actor.typed.scaladsl.adapter._

class ActividadSujetoMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {
  implicit val actor: ActividadSujetoActor = ActividadSujetoActor(ActividadSujetoState())
  val tellSupervisor: ActorRef = TellSupervisor.start(actor.shardActor.toClassic)

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(ActividadSujetoTransaction(tellSupervisor, monitoring))

  override def route: Route =
    (Seq(
      ActividadSujetoStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)

}
