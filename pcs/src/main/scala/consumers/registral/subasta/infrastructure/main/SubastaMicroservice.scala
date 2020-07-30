package consumers.registral.subasta.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import consumers.registral.subasta.infrastructure.dependency_injection.SubastaActor
import consumers.registral.subasta.infrastructure.http.SubastaStateAPI
import consumers.registral.subasta.infrastructure.kafka.SubastaTransaction
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

object SubastaMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(monitoring: Monitoring, ec: ExecutionContext)(implicit system: ActorSystem): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: SubastaActor = SubastaActor()
    implicit val e: ExecutionContext = ec
    Seq(
      SubastaStateAPI(monitoring).route,
      SubastaTransaction(monitoring).route
    ) reduce (_ ~ _)
  }
}
