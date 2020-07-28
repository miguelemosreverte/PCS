package consumers.registral.declaracion_jurada.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import consumers.registral.declaracion_jurada.infrastructure.dependency_injection.DeclaracionJuradaActor
import consumers.registral.declaracion_jurada.infrastructure.http.DeclaracionJuradaStateAPI
import consumers.registral.declaracion_jurada.infrastructure.kafka.DeclaracionJuradaTransaction
import monitoring.Monitoring

object DeclaracionJuradaMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(monitoring: Monitoring)(implicit system: ActorSystem): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem = system.toTyped
    implicit val actor: DeclaracionJuradaActor = DeclaracionJuradaActor()
    Seq(
      DeclaracionJuradaStateAPI(monitoring).route,
      DeclaracionJuradaTransaction(monitoring).route
    ) reduce (_ ~ _)
  }
}
