package consumers.registral.plan_pago.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import consumers.registral.plan_pago.infrastructure.dependency_injection.PlanPagoActor
import consumers.registral.plan_pago.infrastructure.http.PlanPagoStateAPI
import consumers.registral.plan_pago.infrastructure.kafka.{
  PlanPagoNoTributarioTransaction,
  PlanPagoTributarioTransaction
}
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

object PlanPagoMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(monitoring: Monitoring, ec: ExecutionContext)(implicit system: ActorSystem): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: PlanPagoActor = PlanPagoActor()
    implicit val e: ExecutionContext = ec
    Seq(
      PlanPagoStateAPI(monitoring).route,
      PlanPagoTributarioTransaction(monitoring).route,
      PlanPagoNoTributarioTransaction(monitoring).route
    ) reduce (_ ~ _)
  }
}
