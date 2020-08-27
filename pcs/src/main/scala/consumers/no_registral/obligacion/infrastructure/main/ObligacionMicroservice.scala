package consumers.no_registral.obligacion.infrastructure.main

import scala.concurrent.ExecutionContext
import akka.actor.{typed, ActorRef, ActorSystem}
import akka.entity.ShardedEntity.ShardedEntityRequirements
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.no_registral.obligacion.infrastructure.consumer.{
  ObligacionNoTributariaTransaction,
  ObligacionTributariaTransaction
}
import consumers.no_registral.obligacion.infrastructure.http.ObligacionStateAPI
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import design_principles.actor_model.mechanism.QueryStateAPI.QueryStateApiRequirements
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import kafka.KafkaMessageProcessorRequirements

object ObligacionMicroservice extends KafkaConsumerMicroservice {

  def route(m: KafkaConsumerMicroserviceRequirements): Route = {
    val monitoring = m.monitoring

    implicit val shardedEntityR: ShardedEntityRequirements = m.shardedEntityRequirements
    implicit val queryStateApiR: QueryStateApiRequirements = m.queryStateApiRequirements
    implicit val kafkaMessageProcessorR: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    implicit val actorTransactionR: ActorTransaction.ActorTransactionRequirements = m.actorTransactionRequirements

    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val system: ActorSystem = ctx
    implicit val actor: ActorRef = SujetoActor.startWithRequirements(monitoring)

    Seq(
      ObligacionStateAPI(actor, monitoring).route,
      ObligacionTributariaTransaction(actor, monitoring).route,
      ObligacionNoTributariaTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
