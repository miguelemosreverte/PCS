package consumers.no_registral.objeto.infrastructure.main

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.no_registral.objeto.infrastructure.consumer.{
  ObjetoExencionTransaction,
  ObjetoNoTributarioTransaction,
  ObjetoTributarioTransaction,
  ObjetoUpdateNovedadTransaction
}
import consumers.no_registral.objeto.infrastructure.http._
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers.no_registral.sujeto.infrastructure.main.MicroserviceRequirements
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext
object ObjetoMicroservice {

  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    implicit val system: ActorSystem = m.system
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    implicit val actor: ActorRef = SujetoActor.startWithRequirements(monitoring)
    Seq(
      ObjetoStateAPI(actor, monitoring).route,
      ObjetoExencionTransaction(actor, monitoring).route,
      ObjetoNoTributarioTransaction(actor, monitoring).route,
      ObjetoTributarioTransaction(actor, monitoring).route,
      ObjetoUpdateNovedadTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
