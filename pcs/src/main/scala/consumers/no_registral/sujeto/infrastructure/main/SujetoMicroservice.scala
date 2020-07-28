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
import monitoring.Monitoring

object SujetoMicroservice {
  def route(monitoring: Monitoring)(implicit system: ActorSystem): Route = {
    implicit val sujetoActor: ActorRef = SujetoActor.start
    import system.dispatcher
    Seq(
      SujetoStateAPI(monitoring).route,
      SujetoTributarioTransaction(monitoring).routeClassic,
      SujetoNoTributarioTransaction(monitoring).routeClassic
    ) reduce (_ ~ _)
  }

}
