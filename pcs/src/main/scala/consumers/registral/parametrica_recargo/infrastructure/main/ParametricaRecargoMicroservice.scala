package consumers.registral.parametrica_recargo.infrastructure.main

import scala.concurrent.ExecutionContext
import akka.actor.{typed, ActorSystem}
import akka.entity.ShardedEntity.ShardedEntityRequirements
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.parametrica_recargo.infrastructure.dependency_injection.ParametricaRecargoActor
import consumers.registral.parametrica_recargo.infrastructure.http.ParametricaRecargoStateAPI
import consumers.registral.parametrica_recargo.infrastructure.kafka.{
  ParametricaRecargoNoTributarioTransaction,
  ParametricaRecargoTributarioTransaction
}
import design_principles.actor_model.mechanism.QueryStateAPI.QueryStateApiRequirements
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import kafka.KafkaMessageProcessorRequirements

object ParametricaRecargoMicroservice extends KafkaConsumerMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: KafkaConsumerMicroserviceRequirements): Route = {
    val monitoring = m.monitoring

    implicit val shardedEntityR: ShardedEntityRequirements = m.shardedEntityRequirements
    implicit val queryStateApiR: QueryStateApiRequirements = m.queryStateApiRequirements
    implicit val kafkaMessageProcessorR: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    implicit val actorTransactionR: ActorTransaction.ActorTransactionRequirements = m.actorTransactionRequirements

    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._

    implicit val system: akka.actor.typed.ActorSystem[Nothing] = ctx.toTyped
    implicit val classicSystem: akka.actor.ActorSystem = ctx
    implicit val actor: ParametricaRecargoActor = ParametricaRecargoActor()

    Seq(
      ParametricaRecargoStateAPI(actor, monitoring).route,
      ParametricaRecargoTributarioTransaction(actor, monitoring).route,
      ParametricaRecargoNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
