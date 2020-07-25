package consumers.registral.tramite.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import consumers.registral.tramite.infrastructure.dependency_injection.TramiteActor
import consumers.registral.tramite.infrastructure.http.TramiteStateAPI
import consumers.registral.tramite.infrastructure.kafka.TramiteTransaction

object TramiteMicroservice {

  import akka.http.scaladsl.server.Directives._
  def routes(implicit system: ActorSystem): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem = system.toTyped
    implicit val actor = TramiteActor()
    Seq(
      TramiteStateAPI().routes,
      TramiteTransaction().routes
    ) reduce (_ ~ _)
  }
}
