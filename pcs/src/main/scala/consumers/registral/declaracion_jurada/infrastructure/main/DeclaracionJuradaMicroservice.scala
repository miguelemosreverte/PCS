package consumers.registral.declaracion_jurada.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import consumers.registral.declaracion_jurada.infrastructure.dependency_injection.DeclaracionJuradaActor
import consumers.registral.declaracion_jurada.infrastructure.http.DeclaracionJuradaStateAPI
import consumers.registral.declaracion_jurada.infrastructure.kafka.DeclaracionJuradaTransaction
import monitoring.Monitoring

import scala.concurrent.ExecutionContext
import api.actor_transaction.ActorTransaction.Implicits._
import kafka.KafkaMessageProcessorRequirements
object DeclaracionJuradaMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(
      monitoring: Monitoring,
      ec: ExecutionContext
  )(implicit system: ActorSystem, kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: DeclaracionJuradaActor = DeclaracionJuradaActor()
    implicit val e: ExecutionContext = ec
    Seq(
      DeclaracionJuradaStateAPI(actor, monitoring).route,
      DeclaracionJuradaTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
