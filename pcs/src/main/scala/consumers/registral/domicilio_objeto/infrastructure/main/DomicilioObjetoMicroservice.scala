package consumers.registral.domicilio_objeto.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import consumers.registral.domicilio_objeto.infrastructure.dependency_injection.DomicilioObjetoActor
import consumers.registral.domicilio_objeto.infrastructure.http.DomicilioObjetoStateAPI
import consumers.registral.domicilio_objeto.infrastructure.kafka.{
  DomicilioObjetoNoTributarioTransaction,
  DomicilioObjetoTributarioTransaction
}

object DomicilioObjetoMicroservice {

  import akka.http.scaladsl.server.Directives._
  def routes(implicit system: ActorSystem): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem = system.toTyped
    implicit val actor = DomicilioObjetoActor()
    Seq(
      DomicilioObjetoStateAPI().routes,
      DomicilioObjetoTributarioTransaction().routes,
      DomicilioObjetoNoTributarioTransaction().routes
    ) reduce (_ ~ _)
  }
}
