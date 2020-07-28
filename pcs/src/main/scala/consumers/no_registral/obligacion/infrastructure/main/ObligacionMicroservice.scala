package consumers.no_registral.obligacion.infrastructure.main

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import consumers.no_registral.obligacion.infrastructure.consumer.{
  ObligacionNoTributariaTransaction,
  ObligacionTributariaTransaction
}
import consumers.no_registral.obligacion.infrastructure.http.ObligacionStateAPI
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import monitoring.Monitoring

object ObligacionMicroservice {

  def route(monitoring: Monitoring)(implicit system: ActorSystem): Route = {
    implicit val actor: ActorRef = SujetoActor.start
    import system.dispatcher
    Seq(
      ObligacionStateAPI(monitoring).route,
      ObligacionTributariaTransaction(monitoring).routeClassic,
      ObligacionNoTributariaTransaction(monitoring).routeClassic
    ) reduce (_ ~ _)
  }

}
