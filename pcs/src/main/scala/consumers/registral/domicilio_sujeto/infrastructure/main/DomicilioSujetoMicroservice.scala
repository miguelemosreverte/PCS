package consumers.registral.domicilio_sujeto.infrastructure.main

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoState
import consumers.registral.domicilio_sujeto.infrastructure.dependency_injection.DomicilioSujetoActor
import consumers.registral.domicilio_sujeto.infrastructure.http.DomicilioSujetoStateAPI
import consumers.registral.domicilio_sujeto.infrastructure.kafka.DomicilioSujetoNoTributarioTransaction
import consumers.registral.domicilio_sujeto.infrastructure.kafka.DomicilioSujetoTributarioTransaction
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import design_principles.actor_model.mechanism.tell_supervision.TellSupervisor
import akka.actor.typed.scaladsl.adapter._

class DomicilioSujetoMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {
  implicit val actor: DomicilioSujetoActor = DomicilioSujetoActor(DomicilioSujetoState())
  val tellSupervisor: ActorRef = TellSupervisor.start(actor.shardActor.toClassic)

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(DomicilioSujetoNoTributarioTransaction(tellSupervisor, monitoring),
        DomicilioSujetoTributarioTransaction(tellSupervisor, monitoring))

  override def route: Route =
    (Seq(
      DomicilioSujetoStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)

}
