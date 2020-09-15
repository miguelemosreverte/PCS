package consumers.no_registral.objeto.infrastructure.main

import scala.concurrent.ExecutionContext
import akka.actor.{typed, ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndConfig
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.{ActorTransaction, ActorTransactionController}
import consumers.no_registral.objeto.infrastructure.consumer.{
  ObjetoExencionTransaction,
  ObjetoNoTributarioTransaction,
  ObjetoTributarioTransaction,
  ObjetoUpdateNovedadTransaction
}
import consumers.no_registral.objeto.infrastructure.event_processor.ObjetoNovedadCotitularidadProjectionHandler
import consumers.no_registral.objeto.infrastructure.http._
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import design_principles.actor_model.mechanism.QueryStateAPI.QueryStateApiRequirements
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import kafka.KafkaMessageProcessorRequirements
class ObjetoMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {

  implicit val actor: ActorRef = SujetoActor.startWithRequirements(MonitoringAndConfig(monitoring, m.config))

  override def actorTransactions: Set[ActorTransaction[_]] = Set(
    ObjetoExencionTransaction(actor, monitoring),
    ObjetoNoTributarioTransaction(actor, monitoring),
    ObjetoTributarioTransaction(actor, monitoring),
    ObjetoUpdateNovedadTransaction(actor, monitoring)
  )

  def route: Route = {
    val feedbackLoop = ObjetoNovedadCotitularidadProjectionHandler(monitoring, system)
    feedbackLoop.run()

    (Seq(
      feedbackLoop.route,
      ObjetoStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route).toSeq) reduce (_ ~ _)
  }

}
