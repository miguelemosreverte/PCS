package consumers.registral.juicio.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import consumers.registral.juicio.infrastructure.dependency_injection.JuicioActor
import consumers.registral.juicio.infrastructure.http.JuicioStateAPI
import consumers.registral.juicio.infrastructure.kafka.{JuicioNoTributarioTransaction, JuicioTributarioTransaction}
import monitoring.Monitoring

import scala.concurrent.ExecutionContext
import api.actor_transaction.ActorTransaction.Implicits._
import kafka.KafkaMessageProcessorRequirements

object JuicioMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(
      monitoring: Monitoring,
      ec: ExecutionContext
  )(implicit system: ActorSystem, kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: JuicioActor = JuicioActor()
    implicit val e: ExecutionContext = ec
    Seq(
      JuicioStateAPI(actor, monitoring).route,
      JuicioTributarioTransaction(actor, monitoring).route,
      JuicioNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
