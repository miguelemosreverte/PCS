package consumers.registral.parametrica_recargo.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.no_registral.sujeto.infrastructure.main.MicroserviceRequirements
import consumers.registral.parametrica_recargo.infrastructure.dependency_injection.ParametricaRecargoActor
import consumers.registral.parametrica_recargo.infrastructure.http.ParametricaRecargoStateAPI
import consumers.registral.parametrica_recargo.infrastructure.kafka.{
  ParametricaRecargoNoTributarioTransaction,
  ParametricaRecargoTributarioTransaction
}
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

object ParametricaRecargoMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    implicit val system: ActorSystem = m.system
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: ParametricaRecargoActor = ParametricaRecargoActor()

    Seq(
      ParametricaRecargoStateAPI(actor, monitoring).route,
      ParametricaRecargoTributarioTransaction(actor, monitoring).route,
      ParametricaRecargoNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
