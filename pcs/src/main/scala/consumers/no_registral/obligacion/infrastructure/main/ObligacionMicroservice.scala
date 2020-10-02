package consumers.no_registral.obligacion.infrastructure.main

import scala.concurrent.ExecutionContext
import akka.actor.{typed, ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.no_registral.obligacion.infrastructure.consumer.{
  ObligacionNoTributariaTransaction,
  ObligacionTributariaTransaction
}
import consumers.no_registral.obligacion.infrastructure.http.ObligacionStateAPI
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import design_principles.actor_model.mechanism.QueryStateAPI.QueryStateApiRequirements
import design_principles.actor_model.mechanism.tell_supervision.TellSupervisor
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
import kafka.KafkaMessageProcessorRequirements

class ObligacionMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {

  implicit val actor: ActorRef = SujetoActor.startWithRequirements(monitoringAndMessageProducer)

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      ObligacionTributariaTransaction(actor, monitoring),
      ObligacionNoTributariaTransaction(actor, monitoring)
    )

  def route: Route = {
    (Seq(
      ObligacionStateAPI(actor, monitoring).route
    ) ++
    actorTransactions.map(_.route).toSeq) reduce (_ ~ _)
  }

}
