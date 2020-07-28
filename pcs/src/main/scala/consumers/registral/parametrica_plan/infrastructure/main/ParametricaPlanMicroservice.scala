package consumers.registral.parametrica_plan.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import consumers.registral.parametrica_plan.infrastructure.dependency_injection.ParametricaPlanActor
import consumers.registral.parametrica_plan.infrastructure.http.ParametricaPlanStateAPI
import consumers.registral.parametrica_plan.infrastructure.kafka.{
  ParametricaPlanNoTributarioTransaction,
  ParametricaPlanTributarioTransaction
}
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

object ParametricaPlanMicroservice {

  import akka.http.scaladsl.server.Directives._
  def routes(implicit system: ActorSystem, monitoring: Monitoring, ec: ExecutionContext): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem = system.toTyped
    implicit val actor = ParametricaPlanActor()
    Seq(
      ParametricaPlanStateAPI().routes,
      ParametricaPlanTributarioTransaction().routes,
      ParametricaPlanNoTributarioTransaction().routes
    ) reduce (_ ~ _)
  }
}
