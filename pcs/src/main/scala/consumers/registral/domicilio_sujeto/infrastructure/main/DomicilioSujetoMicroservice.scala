package consumers.registral.domicilio_sujeto.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import consumers.registral.domicilio_sujeto.infrastructure.dependency_injection.DomicilioSujetoActor
import consumers.registral.domicilio_sujeto.infrastructure.http.DomicilioSujetoStateAPI
import consumers.registral.domicilio_sujeto.infrastructure.kafka.{
  DomicilioSujetoNoTributarioTransaction,
  DomicilioSujetoTributarioTransaction
}
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

object DomicilioSujetoMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(monitoring: Monitoring, ec: ExecutionContext)(implicit system: ActorSystem): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: DomicilioSujetoActor = DomicilioSujetoActor()
    implicit val e: ExecutionContext = ec
    Seq(
      DomicilioSujetoStateAPI(monitoring).route,
      DomicilioSujetoTributarioTransaction(monitoring).route,
      DomicilioSujetoNoTributarioTransaction(monitoring).route
    ) reduce (_ ~ _)
  }
}
