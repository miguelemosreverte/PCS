package consumers.registral.domicilio_objeto.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import consumers.registral.domicilio_objeto.infrastructure.dependency_injection.DomicilioObjetoActor
import consumers.registral.domicilio_objeto.infrastructure.http.DomicilioObjetoStateAPI
import consumers.registral.domicilio_objeto.infrastructure.kafka.{
  DomicilioObjetoNoTributarioTransaction,
  DomicilioObjetoTributarioTransaction
}
import monitoring.Monitoring

import scala.concurrent.ExecutionContext
import api.actor_transaction.ActorTransaction.Implicits._
import kafka.KafkaMessageProcessorRequirements

object DomicilioObjetoMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(
      monitoring: Monitoring,
      ec: ExecutionContext
  )(implicit system: ActorSystem, kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: DomicilioObjetoActor = DomicilioObjetoActor()
    implicit val e: ExecutionContext = ec
    Seq(
      DomicilioObjetoStateAPI(actor, monitoring).route,
      DomicilioObjetoTributarioTransaction(actor, monitoring).route,
      DomicilioObjetoNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
