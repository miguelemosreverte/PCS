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
import kafka.KafkaMessageProcessorRequirements
import monitoring.Monitoring

import scala.concurrent.ExecutionContext
import api.actor_transaction.ActorTransaction.Implicits._
object ObjetoMicroservice {

  def route(
      monitoring: Monitoring,
      ec: ExecutionContext
  )(implicit system: ActorSystem, kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements): Route = {
    implicit val actor: ActorRef = SujetoActor.startWithRequirements(monitoring)
    implicit val e: ExecutionContext = ec
    Seq(
      ObjetoStateAPI(actor, monitoring).route,
      ObjetoExencionTransaction(actor, monitoring).route,
      ObjetoNoTributarioTransaction(actor, monitoring).route,
      ObjetoTributarioTransaction(actor, monitoring).route,
      ObjetoUpdateNovedadTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
