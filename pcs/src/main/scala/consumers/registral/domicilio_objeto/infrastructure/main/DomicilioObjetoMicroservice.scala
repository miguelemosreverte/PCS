package consumers.registral.domicilio_objeto.infrastructure.main

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.domicilio_objeto.domain.DomicilioObjetoState
import consumers.registral.domicilio_objeto.infrastructure.dependency_injection.DomicilioObjetoActor
import consumers.registral.domicilio_objeto.infrastructure.http.DomicilioObjetoStateAPI
import consumers.registral.domicilio_objeto.infrastructure.kafka.DomicilioObjetoNoTributarioTransaction
import consumers.registral.domicilio_objeto.infrastructure.kafka.DomicilioObjetoTributarioTransaction
import design_principles.actor_model.mechanism.tell_supervision.TellSupervisor
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import akka.actor.typed.scaladsl.adapter._

class DomicilioObjetoMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {
  implicit val actor: DomicilioObjetoActor = DomicilioObjetoActor(DomicilioObjetoState())
  val tellSupervisor: ActorRef = TellSupervisor.start(actor.shardActor.toClassic)

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      DomicilioObjetoNoTributarioTransaction(actor.shardActor.toClassic, monitoring),
      DomicilioObjetoTributarioTransaction(actor.shardActor.toClassic, monitoring)
    )

  override def route: Route =
    (Seq(
      DomicilioObjetoStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)

}
