package consumers.registral.subasta.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction.Implicits._
import consumers.no_registral.sujeto.infrastructure.main.MicroserviceRequirements
import consumers.registral.subasta.infrastructure.dependency_injection.SubastaActor
import consumers.registral.subasta.infrastructure.http.SubastaStateAPI
import consumers.registral.subasta.infrastructure.kafka.SubastaTransaction
import kafka.KafkaMessageProcessorRequirements

import scala.concurrent.ExecutionContext

object SubastaMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(m: MicroserviceRequirements): Route = {
    val monitoring = m.monitoring
    implicit val ec: ExecutionContext = m.executionContext
    implicit val system: ActorSystem = m.system
    implicit val kafkaProcesorRequirements: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: SubastaActor = SubastaActor()
    Seq(
      SubastaStateAPI(actor, monitoring).route,
      SubastaTransaction(actor, monitoring).route
    ) reduce (_ ~ _)
  }
}
