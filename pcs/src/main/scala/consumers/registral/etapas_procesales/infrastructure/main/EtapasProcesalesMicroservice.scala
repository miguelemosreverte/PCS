package consumers.registral.etapas_procesales.infrastructure.main

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.etapas_procesales.domain.EtapasProcesalesState
import consumers.registral.etapas_procesales.infrastructure.dependency_injection.EtapasProcesalesActor
import consumers.registral.etapas_procesales.infrastructure.http.EtapasProcesalesStateAPI
import consumers.registral.etapas_procesales.infrastructure.kafka.EtapasProcesalesNoTributarioTransaction
import consumers.registral.etapas_procesales.infrastructure.kafka.EtapasProcesalesTributarioTransaction
import design_principles.actor_model.mechanism.tell_supervision.TellSupervisor
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import akka.actor.typed.scaladsl.adapter._

class EtapasProcesalesMicroservice(implicit m: KafkaConsumerMicroserviceRequirements)
    extends KafkaConsumerMicroservice {
  implicit val actor: EtapasProcesalesActor = EtapasProcesalesActor(EtapasProcesalesState())
  val tellSupervisor: ActorRef = TellSupervisor.start(actor.shardActor.toClassic)

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      EtapasProcesalesNoTributarioTransaction(actor.shardActor.toClassic, monitoring),
      EtapasProcesalesTributarioTransaction(actor.shardActor.toClassic, monitoring)
    )

  override def route: Route =
    (Seq(
      EtapasProcesalesStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)

}
