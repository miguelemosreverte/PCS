package consumers.no_registral.objeto.infrastructure.main

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import consumers.no_registral.objeto.infrastructure.consumer.{
  ObjetoExencionTransaction,
  ObjetoNoTributarioTransaction,
  ObjetoTributarioTransaction,
  ObjetoUpdateNovedadTransaction
}
import consumers.no_registral.objeto.infrastructure.http._
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor

object ObjetoMicroservice {

  def routes(implicit system: ActorSystem): Route = {
    implicit val actor: ActorRef = SujetoActor.start
    Seq(
      ObjetoStateAPI().routes,
      ObjetoExencionTransaction().routesClassic,
      ObjetoNoTributarioTransaction().routesClassic,
      ObjetoTributarioTransaction().routesClassic,
      ObjetoUpdateNovedadTransaction().routesClassic
    ) reduce (_ ~ _)
  }
}
