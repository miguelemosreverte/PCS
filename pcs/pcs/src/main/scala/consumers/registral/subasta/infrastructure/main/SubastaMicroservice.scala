package consumers.registral.subasta.infrastructure.main

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import consumers.registral.subasta.infrastructure.dependency_injection.SubastaActor
import consumers.registral.subasta.infrastructure.http.SubastaStateAPI
import consumers.registral.subasta.infrastructure.kafka.SubastaTransaction

object SubastaMicroservice {

  import akka.http.scaladsl.server.Directives._
  def routes(implicit system: ActorSystem): Route = {
    import akka.actor.typed.scaladsl.adapter._

    implicit val typedSystem = system.toTyped
    implicit val actor = SubastaActor()
    Seq(
      SubastaStateAPI().routes,
      SubastaTransaction().routes
    ) reduce (_ ~ _)
  }
}
