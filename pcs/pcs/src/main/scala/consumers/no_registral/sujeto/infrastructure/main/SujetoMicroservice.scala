package consumers.no_registral.sujeto.infrastructure.main

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import consumers.no_registral.sujeto.infrastructure.consumer.{
  SujetoNoTributarioTransaction,
  SujetoTributarioTransaction
}
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers.no_registral.sujeto.infrastructure.http.SujetoStateAPI

object SujetoMicroservice {
  def routes(implicit system: ActorSystem): Route = {
    implicit val sujetoActor: ActorRef = SujetoActor.start

    Seq(
      SujetoStateAPI().routes,
      SujetoTributarioTransaction().routesClassic,
      SujetoNoTributarioTransaction().routesClassic
    ) reduce (_ ~ _)
  }

}
