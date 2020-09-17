package consumers.no_registral.cotitularidad.infrastructure.main

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.ProductionMonitoringAndMessageProducer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.no_registral.cotitularidad.infrastructure.dependency_injection.CotitularidadActor
import consumers.no_registral.cotitularidad.infrastructure.http.CotitularidadStateAPI
import consumers.no_registral.cotitularidad.infrastructure.kafka.{
  AddSujetoCotitularTransaction,
  CotitularPublishSnapshotTransaction
}
import design_principles.actor_model.mechanism.QueryStateAPI.QueryStateApiRequirements
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import design_principles.microservice.{Microservice, MicroserviceRequirements}
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

class CotitularidadMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {

  implicit val actor: ActorRef =
    CotitularidadActor.startWithRequirements(monitoringAndMessageProducer)

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      AddSujetoCotitularTransaction(actor, monitoring),
      CotitularPublishSnapshotTransaction(actor, monitoring)
    )

  override def route: Route =
    (Seq(CotitularidadStateAPI(actor, monitoring).route) ++ actorTransactions.map(_.route)) reduce (_ ~ _)
}
