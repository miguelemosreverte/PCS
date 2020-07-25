package consumers.registral.domicilio_sujeto.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import consumers.registral.domicilio_sujeto.infrastructure.dependency_injection.DomicilioSujetoActor
import consumers.registral.domicilio_sujeto.infrastructure.http.DomicilioSujetoStateAPI
import consumers.registral.domicilio_sujeto.infrastructure.kafka.{
  DomicilioSujetoNoTributarioTransaction,
  DomicilioSujetoTributarioTransaction
}

object DomicilioSujetoMicroservice {

  import akka.http.scaladsl.server.Directives._
  def routes(implicit system: ActorSystem): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem = system.toTyped
    implicit val actor = DomicilioSujetoActor()
    Seq(
      DomicilioSujetoStateAPI().routes,
      DomicilioSujetoTributarioTransaction().routes,
      DomicilioSujetoNoTributarioTransaction().routes
    ) reduce (_ ~ _)
  }
}
