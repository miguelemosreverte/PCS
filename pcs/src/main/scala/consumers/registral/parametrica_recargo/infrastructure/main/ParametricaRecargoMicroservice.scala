package consumers.registral.parametrica_recargo.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import consumers.registral.parametrica_recargo.infrastructure.dependency_injection.ParametricaRecargoActor
import consumers.registral.parametrica_recargo.infrastructure.http.ParametricaRecargoStateAPI
import consumers.registral.parametrica_recargo.infrastructure.kafka.{
  ParametricaRecargoNoTributarioTransaction,
  ParametricaRecargoTributarioTransaction
}

object ParametricaRecargoMicroservice {

  import akka.http.scaladsl.server.Directives._
  def routes(implicit system: ActorSystem): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem = system.toTyped
    implicit val actor = ParametricaRecargoActor()

    Seq(
      ParametricaRecargoStateAPI().routes,
      ParametricaRecargoTributarioTransaction().routes,
      ParametricaRecargoNoTributarioTransaction().routes
    ) reduce (_ ~ _)
  }
}
