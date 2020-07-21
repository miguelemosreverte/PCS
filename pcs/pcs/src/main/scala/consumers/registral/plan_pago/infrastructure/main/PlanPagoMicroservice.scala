package consumers.registral.plan_pago.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import consumers.registral.plan_pago.infrastructure.dependency_injection.PlanPagoActor
import consumers.registral.plan_pago.infrastructure.http.PlanPagoStateAPI
import consumers.registral.plan_pago.infrastructure.kafka.{
  PlanPagoNoTributarioTransaction,
  PlanPagoTributarioTransaction
}

object PlanPagoMicroservice {

  import akka.http.scaladsl.server.Directives._
  def routes(implicit system: ActorSystem): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem = system.toTyped
    implicit val actor = PlanPagoActor()
    Seq(
      PlanPagoStateAPI().routes,
      PlanPagoTributarioTransaction().routes,
      PlanPagoNoTributarioTransaction().routes
    ) reduce (_ ~ _)
  }
}
