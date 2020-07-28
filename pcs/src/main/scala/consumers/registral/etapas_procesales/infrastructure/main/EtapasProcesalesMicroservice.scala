package consumers.registral.etapas_procesales.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import consumers.registral.etapas_procesales.infrastructure.dependency_injection.EtapasProcesalesActor
import consumers.registral.etapas_procesales.infrastructure.http.EtapasProcesalesStateAPI
import consumers.registral.etapas_procesales.infrastructure.kafka.{
  EtapasProcesalesNoTributarioTransaction,
  EtapasProcesalesTributarioTransaction
}
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

object EtapasProcesalesMicroservice {

  import akka.http.scaladsl.server.Directives._
  def routes(implicit system: ActorSystem, monitoring: Monitoring, ec: ExecutionContext): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem = system.toTyped
    implicit val actor = EtapasProcesalesActor()
    Seq(
      EtapasProcesalesStateAPI().routes,
      EtapasProcesalesTributarioTransaction().routes,
      EtapasProcesalesNoTributarioTransaction().routes
    ) reduce (_ ~ _)
  }
}
