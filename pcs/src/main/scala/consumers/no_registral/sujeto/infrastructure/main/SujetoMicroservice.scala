package consumers.no_registral.sujeto.infrastructure.main

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import consumers.no_registral.sujeto.infrastructure.consumer.{
  SujetoNoTributarioTransaction,
  SujetoTributarioTransaction
}
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers.no_registral.sujeto.infrastructure.http.SujetoStateAPI
import monitoring.Monitoring

import scala.concurrent.ExecutionContext
import api.actor_transaction.ActorTransaction.Implicits._
import kafka.KafkaMessageProcessorRequirements
case class MicroserviceRequirements(monitoring: Monitoring,
                                    executionContext: ExecutionContext,
                                    system: ActorSystem,
                                    kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements)
object SujetoMicroservice {
  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    implicit val system: ActorSystem = m.system
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    implicit val actor: ActorRef = SujetoActor.startWithRequirements(monitoring)
    Seq(
      SujetoStateAPI(actor, monitoring).route,
      SujetoTributarioTransaction(actor, monitoring).route,
      SujetoNoTributarioTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }

}
