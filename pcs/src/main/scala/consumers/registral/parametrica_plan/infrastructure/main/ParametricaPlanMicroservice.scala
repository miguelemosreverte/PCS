package consumers.registral.parametrica_plan.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.no_registral.sujeto.infrastructure.main.MicroserviceRequirements
import consumers.registral.parametrica_plan.infrastructure.dependency_injection.ParametricaPlanActor
import consumers.registral.parametrica_plan.infrastructure.http.ParametricaPlanStateAPI
import consumers.registral.parametrica_plan.infrastructure.kafka.{
  ParametricaPlanNoTributarioTransaction,
  ParametricaPlanTributarioTransaction
}
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

object ParametricaPlanMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    implicit val system: ActorSystem = m.system
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: ParametricaPlanActor = ParametricaPlanActor()
    Seq(
      ParametricaPlanStateAPI(actor, monitoring).route,
      ParametricaPlanTributarioTransaction(actor, monitoring).route,
      ParametricaPlanNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
