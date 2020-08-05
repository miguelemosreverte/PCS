package consumers.registral.domicilio_sujeto.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.no_registral.sujeto.infrastructure.main.MicroserviceRequirements
import consumers.registral.domicilio_sujeto.infrastructure.dependency_injection.DomicilioSujetoActor
import consumers.registral.domicilio_sujeto.infrastructure.http.DomicilioSujetoStateAPI
import consumers.registral.domicilio_sujeto.infrastructure.kafka.{
  DomicilioSujetoNoTributarioTransaction,
  DomicilioSujetoTributarioTransaction
}
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

object DomicilioSujetoMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    implicit val system: ActorSystem = m.system
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: DomicilioSujetoActor = DomicilioSujetoActor()
    Seq(
      DomicilioSujetoStateAPI(actor, monitoring).route,
      DomicilioSujetoTributarioTransaction(actor, monitoring).route,
      DomicilioSujetoNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
