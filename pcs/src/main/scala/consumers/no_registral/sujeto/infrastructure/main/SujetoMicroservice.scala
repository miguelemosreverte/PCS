package consumers.no_registral.sujeto.infrastructure.main

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.no_registral.sujeto.infrastructure.consumer.{
  SujetoNoTributarioTransaction,
  SujetoTributarioTransaction
}
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers.no_registral.sujeto.infrastructure.http.SujetoStateAPI
import design_principles.microservice.{Microservice, MicroserviceRequirements}
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

object SujetoMicroservice extends Microservice {
  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    val ctx = m.ctx
    import akka.actor.typed.scaladsl.adapter._
    implicit val systemTyped = ctx.system
    implicit val system: ActorSystem = ctx.system.toClassic
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    implicit val actor: ActorRef = SujetoActor.startWithRequirements(monitoring)
    Seq(
      SujetoStateAPI(actor, monitoring).route,
      SujetoTributarioTransaction(actor, monitoring).route,
      SujetoNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }

}
