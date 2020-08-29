package consumers.registral.actividad_sujeto.infrastructure.main

import scala.concurrent.ExecutionContext
import akka.actor.typed
import akka.actor.typed.ActorSystem
import akka.entity.ShardedEntity.ShardedEntityRequirements
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.actividad_sujeto.domain.ActividadSujetoState
import consumers.registral.actividad_sujeto.infrastructure.dependency_injection.ActividadSujetoActor
import consumers.registral.actividad_sujeto.infrastructure.http.ActividadSujetoStateAPI
import consumers.registral.actividad_sujeto.infrastructure.kafka.ActividadSujetoTransaction
import design_principles.actor_model.mechanism.QueryStateAPI.QueryStateApiRequirements
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import kafka.KafkaMessageProcessorRequirements

object ActividadSujetoMicroservice extends KafkaConsumerMicroservice {

  def route(m: KafkaConsumerMicroserviceRequirements): Route = {
    val monitoring = m.monitoring

    implicit val shardedEntityR: ShardedEntityRequirements = m.shardedEntityRequirements
    implicit val queryStateApiR: QueryStateApiRequirements = m.queryStateApiRequirements
    implicit val kafkaMessageProcessorR: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    implicit val actorTransactionR: ActorTransaction.ActorTransactionRequirements = m.actorTransactionRequirements

    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val system: ActorSystem[Nothing] = ctx.toTyped
    implicit val classicSystem: akka.actor.ActorSystem = ctx
    implicit val actor: ActividadSujetoActor = ActividadSujetoActor(ActividadSujetoState(), m.config)
    Seq(
      ActividadSujetoStateAPI(actor, monitoring).route,
      ActividadSujetoTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }

}
