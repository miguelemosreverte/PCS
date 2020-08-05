package consumers.registral.parametrica_recargo.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import consumers.registral.parametrica_recargo.infrastructure.dependency_injection.ParametricaRecargoActor
import consumers.registral.parametrica_recargo.infrastructure.http.ParametricaRecargoStateAPI
import consumers.registral.parametrica_recargo.infrastructure.kafka.{
  ParametricaRecargoNoTributarioTransaction,
  ParametricaRecargoTributarioTransaction
}
import monitoring.Monitoring

import scala.concurrent.ExecutionContext
import api.actor_transaction.ActorTransaction.Implicits._
import kafka.KafkaMessageProcessorRequirements

object ParametricaRecargoMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(
      monitoring: Monitoring,
      ec: ExecutionContext
  )(implicit system: ActorSystem, kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: ParametricaRecargoActor = ParametricaRecargoActor()
    implicit val e: ExecutionContext = ec

    Seq(
      ParametricaRecargoStateAPI(actor, monitoring).route,
      ParametricaRecargoTributarioTransaction(actor, monitoring).route,
      ParametricaRecargoNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
