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

import scala.concurrent.ExecutionContext

object SujetoMicroservice {
  def route(monitoring: Monitoring, ec: ExecutionContext)(implicit system: ActorSystem): Route = {
    implicit val actor: ActorRef = SujetoActor.start
    implicit val e: ExecutionContext = ec
    Seq(
      SujetoStateAPI(actor, monitoring).route,
      SujetoTributarioTransaction(actor, monitoring).routeClassic,
      SujetoNoTributarioTransaction(actor, monitoring).routeClassic
    ) reduce (_ ~ _)
  }

}
