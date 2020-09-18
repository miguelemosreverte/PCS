package consumers.no_registral.objeto.infrastructure.main

import scala.concurrent.ExecutionContext
import akka.actor.{typed, ActorRef, ActorSystem}
import akka.entity.ShardedEntity.{MonitoringAndMessageProducer, ShardedEntityRequirements}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.no_registral.objeto.infrastructure.consumer.{
  ObjetoExencionTransaction,
  ObjetoNoTributarioTransaction,
  ObjetoTributarioTransaction,
  ObjetoUpdateNovedadTransaction
}
import consumers.no_registral.objeto.infrastructure.http._
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import design_principles.actor_model.mechanism.QueryStateAPI.QueryStateApiRequirements
import design_principles.actor_model.mechanism.tell_supervision.TellSupervisor
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
class ObjetoMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {

  implicit val actor: ActorRef =
    SujetoActor.startWithRequirements(monitoringAndMessageProducer)

  val tellSupervisor: ActorRef = TellSupervisor.start(actor)

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      ObjetoExencionTransaction(tellSupervisor, monitoring),
      ObjetoNoTributarioTransaction(tellSupervisor, monitoring),
      ObjetoTributarioTransaction(tellSupervisor, monitoring),
      ObjetoUpdateNovedadTransaction(tellSupervisor, monitoring)
    )
  def route: Route =
    (
      Set(
        ObjetoStateAPI(actor, monitoring).route
      ) ++ actorTransactions.map(_.route)
    ).reduce(_ ~ _)

}
