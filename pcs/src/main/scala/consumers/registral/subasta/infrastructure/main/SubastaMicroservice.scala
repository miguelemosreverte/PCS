package consumers.registral.subasta.infrastructure.main

import scala.concurrent.ExecutionContext
import akka.actor.{typed, ActorSystem}
import akka.entity.ShardedEntity.ShardedEntityRequirements
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.subasta.domain.SubastaState
import consumers.registral.subasta.infrastructure.dependency_injection.SubastaActor
import consumers.registral.subasta.infrastructure.http.SubastaStateAPI
import consumers.registral.subasta.infrastructure.kafka.SubastaTransaction
import design_principles.actor_model.mechanism.QueryStateAPI.QueryStateApiRequirements
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import kafka.KafkaMessageProcessorRequirements

object SubastaMicroservice extends KafkaConsumerMicroservice {

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
    implicit val actor: SubastaActor = SubastaActor(SubastaState(), m.config)
    Seq(
      SubastaStateAPI(actor, monitoring).route,
      SubastaTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
