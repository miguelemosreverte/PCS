package consumers.registral.domicilio_objeto.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.no_registral.sujeto.infrastructure.main.MicroserviceRequirements
import consumers.registral.domicilio_objeto.infrastructure.dependency_injection.DomicilioObjetoActor
import consumers.registral.domicilio_objeto.infrastructure.http.DomicilioObjetoStateAPI
import consumers.registral.domicilio_objeto.infrastructure.kafka.{
  DomicilioObjetoNoTributarioTransaction,
  DomicilioObjetoTributarioTransaction
}
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

object DomicilioObjetoMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    implicit val system: ActorSystem = m.system
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: DomicilioObjetoActor = DomicilioObjetoActor()
    Seq(
      DomicilioObjetoStateAPI(actor, monitoring).route,
      DomicilioObjetoTributarioTransaction(actor, monitoring).route,
      DomicilioObjetoNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
