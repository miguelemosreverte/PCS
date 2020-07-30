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

import scala.concurrent.ExecutionContext

object ObligacionMicroservice {

  def route(monitoring: Monitoring, ec: ExecutionContext)(implicit system: ActorSystem): Route = {
    implicit val actor: ActorRef = SujetoActor.start
    implicit val e: ExecutionContext = ec
    Seq(
      ObligacionStateAPI(actor, monitoring).route,
      ObligacionTributariaTransaction(actor, monitoring).routeClassic,
      ObligacionNoTributariaTransaction(actor, monitoring).routeClassic
    ) reduce (_ ~ _)
  }

}
