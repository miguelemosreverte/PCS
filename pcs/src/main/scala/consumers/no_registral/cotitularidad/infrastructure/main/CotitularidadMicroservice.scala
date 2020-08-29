package consumers.no_registral.cotitularidad.infrastructure.main

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.ShardedEntityRequirements
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.no_registral.cotitularidad.infrastructure.dependency_injection.CotitularidadActor
import consumers.no_registral.cotitularidad.infrastructure.http.CotitularidadStateAPI
import consumers.no_registral.cotitularidad.infrastructure.kafka.{
  AddCotitularTransaction,
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

object CotitularidadMicroservice extends KafkaConsumerMicroservice {

  def route(m: KafkaConsumerMicroserviceRequirements): Route = {
    val monitoring = m.monitoring

    implicit val shardedEntityR: ShardedEntityRequirements = m.shardedEntityRequirements
    implicit val queryStateApiR: QueryStateApiRequirements = m.queryStateApiRequirements
    implicit val kafkaMessageProcessorR: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    implicit val actorTransactionR: ActorTransaction.ActorTransactionRequirements = m.actorTransactionRequirements

    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val system: ActorSystem = ctx
    implicit val actor: ActorRef =
      CotitularidadActor.startWithRequirements(kafkaMessageProcessorR)
    Seq(
      CotitularidadStateAPI(actor, monitoring).route,
      AddCotitularTransaction(actor, monitoring).route,
      CotitularPublishSnapshotTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
