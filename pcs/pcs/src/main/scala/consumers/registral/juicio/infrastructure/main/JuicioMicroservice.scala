package consumers.registral.juicio.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import consumers.registral.juicio.infrastructure.dependency_injection.JuicioActor
import consumers.registral.juicio.infrastructure.http.JuicioStateAPI
import consumers.registral.juicio.infrastructure.kafka.{JuicioNoTributarioTransaction, JuicioTributarioTransaction}

object JuicioMicroservice {

  import akka.http.scaladsl.server.Directives._
  def routes(implicit system: ActorSystem): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem = system.toTyped
    implicit val actor = JuicioActor()
    Seq(
      JuicioStateAPI().routes,
      JuicioTributarioTransaction().routes,
      JuicioNoTributarioTransaction().routes
    ) reduce (_ ~ _)
  }
}
