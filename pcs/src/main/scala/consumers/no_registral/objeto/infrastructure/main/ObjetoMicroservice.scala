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
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

object ObjetoMicroservice {

  def route(monitoring: Monitoring, ec: ExecutionContext)(implicit system: ActorSystem): Route = {
    implicit val actor: ActorRef = SujetoActor.start
    implicit val e: ExecutionContext = ec
    Seq(
      ObjetoStateAPI(actor, monitoring).route,
      ObjetoExencionTransaction(actor, monitoring).routeClassic,
      ObjetoNoTributarioTransaction(actor, monitoring).routeClassic,
      ObjetoTributarioTransaction(actor, monitoring).routeClassic,
      ObjetoUpdateNovedadTransaction(actor, monitoring).routeClassic
    ) reduce (_ ~ _)
  }
}
