package consumers.registral.parametrica_plan.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import consumers.registral.parametrica_plan.infrastructure.dependency_injection.ParametricaPlanActor
import consumers.registral.parametrica_plan.infrastructure.http.ParametricaPlanStateAPI
import consumers.registral.parametrica_plan.infrastructure.kafka.{
  ParametricaPlanNoTributarioTransaction,
  ParametricaPlanTributarioTransaction
}
import monitoring.Monitoring

import scala.concurrent.ExecutionContext
import api.actor_transaction.ActorTransaction.Implicits._
import kafka.KafkaMessageProcessorRequirements

object ParametricaPlanMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(
      monitoring: Monitoring,
      ec: ExecutionContext
  )(implicit system: ActorSystem, kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: ParametricaPlanActor = ParametricaPlanActor()
    implicit val e: ExecutionContext = ec
    Seq(
      ParametricaPlanStateAPI(actor, monitoring).route,
      ParametricaPlanTributarioTransaction(actor, monitoring).route,
      ParametricaPlanNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
