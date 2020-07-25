package consumers.registral.tramite.infrastructure.http

import java.time.LocalDateTime

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import components.QueryStateAPI
import consumers.registral.tramite.application.entities.TramiteQueries.GetStateTramite
import consumers.registral.tramite.infrastructure.dependency_injection.TramiteActor
import consumers.registral.tramite.infrastructure.json._

case class TramiteStateAPI()(implicit actor: TramiteActor, system: akka.actor.typed.ActorSystem[_])
    extends QueryStateAPI {
  import TramiteStateAPI._
  def getState: Route =
    withSujeto { sujetoId =>
      withTramite { tramiteId =>
        queryState(actor, GetStateTramite(sujetoId, tramiteId))(
          GetTramiteResponseF,
          state => state.fechaUltMod == LocalDateTime.MIN
        )

      }
    }

  def routes: Route = GET(getState)
}

object TramiteStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withSujeto: (String => Route) => Route = nestedRoute("sujeto") _
  def withTramite = path("tramite" / Segment)
}
