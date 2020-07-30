package consumers.registral.domicilio_objeto.infrastructure.main

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import consumers.registral.domicilio_objeto.infrastructure.dependency_injection.DomicilioObjetoActor
import consumers.registral.domicilio_objeto.infrastructure.http.DomicilioObjetoStateAPI
import consumers.registral.domicilio_objeto.infrastructure.kafka.{
  DomicilioObjetoNoTributarioTransaction,
  DomicilioObjetoTributarioTransaction
}
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

object DomicilioObjetoMicroservice {

  import akka.http.scaladsl.server.Directives._
  def route(monitoring: Monitoring, ec: ExecutionContext)(implicit system: ActorSystem): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
    implicit val actor: DomicilioObjetoActor = DomicilioObjetoActor()
    implicit val e: ExecutionContext = ec
    Seq(
      DomicilioObjetoStateAPI(monitoring).route,
      DomicilioObjetoTributarioTransaction(monitoring).route,
      DomicilioObjetoNoTributarioTransaction(monitoring).route
    ) reduce (_ ~ _)
  }
}
