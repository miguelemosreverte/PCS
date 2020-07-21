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

object ObligacionMicroservice {

  def routes(implicit system: ActorSystem): Route = {
    implicit val actor: ActorRef = SujetoActor.start

    Seq(
      ObligacionStateAPI().routes,
      ObligacionTributariaTransaction().routesClassic,
      ObligacionNoTributariaTransaction().routesClassic
    ) reduce (_ ~ _)
  }

}
