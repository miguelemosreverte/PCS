package consumers.registral.juicio.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.no_registral.sujeto.infrastructure.main.MicroserviceRequirements
import consumers.registral.juicio.infrastructure.dependency_injection.JuicioActor
import consumers.registral.juicio.infrastructure.http.JuicioStateAPI
import consumers.registral.juicio.infrastructure.kafka.{JuicioNoTributarioTransaction, JuicioTributarioTransaction}
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

object JuicioMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    implicit val system: ActorSystem = m.system
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: JuicioActor = JuicioActor()
    Seq(
      JuicioStateAPI(actor, monitoring).route,
      JuicioTributarioTransaction(actor, monitoring).route,
      JuicioNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
