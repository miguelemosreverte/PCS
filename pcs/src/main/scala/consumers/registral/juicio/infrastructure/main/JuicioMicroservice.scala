package consumers.registral.juicio.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import consumers.registral.juicio.infrastructure.dependency_injection.JuicioActor
import consumers.registral.juicio.infrastructure.http.JuicioStateAPI
import consumers.registral.juicio.infrastructure.kafka.{JuicioNoTributarioTransaction, JuicioTributarioTransaction}
import monitoring.Monitoring

object JuicioMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(monitoring: Monitoring)(implicit system: ActorSystem): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem = system.toTyped
    implicit val actor = JuicioActor()
    Seq(
      JuicioStateAPI(monitoring).route,
      JuicioTributarioTransaction(monitoring).route,
      JuicioNoTributarioTransaction(monitoring).route
    ) reduce (_ ~ _)
  }
}
