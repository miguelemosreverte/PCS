package consumers.registral.actividad_sujeto.infrastructure.main

import akka.actor.typed.scaladsl.adapter._
import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import consumers.registral.actividad_sujeto.infrastructure.dependency_injection.ActividadSujetoActor
import consumers.registral.actividad_sujeto.infrastructure.http.ActividadSujetoStateAPI
import consumers.registral.actividad_sujeto.infrastructure.kafka.ActividadSujetoTransaction
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

object ActividadSujetoMicroservice {

  def routes(implicit system: ActorSystem, monitoring: Monitoring, ec: ExecutionContext): Route = {
    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: ActividadSujetoActor = ActividadSujetoActor()

    Seq(
      ActividadSujetoStateAPI().routes,
      ActividadSujetoTransaction().routes
    ) reduce (_ ~ _)
  }

}
