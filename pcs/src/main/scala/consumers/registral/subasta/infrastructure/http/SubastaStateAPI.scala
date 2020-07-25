package consumers.registral.subasta.infrastructure.http

import java.time.LocalDateTime

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import components.QueryStateAPI
import consumers.registral.subasta.application.entities.SubastaQueries.GetStateSubasta
import consumers.registral.subasta.infrastructure.dependency_injection.SubastaActor
import consumers.registral.subasta.infrastructure.json._

case class SubastaStateAPI()(implicit actor: SubastaActor, system: akka.actor.typed.ActorSystem[_])
    extends QueryStateAPI {
  import SubastaStateAPI._
  def getState: Route =
    withSujeto { sujetoId =>
      withObjeto { objetoId =>
        withTipoObjeto { tipoObjeto =>
          withSubasta { subastaId =>
            queryState(actor,
                       GetStateSubasta(
                         sujetoId,
                         objetoId,
                         tipoObjeto,
                         subastaId
                       ))(GetSubastaResponseF, state => state.fechaUltMod == LocalDateTime.MIN)
          }
        }
      }
    }

  def routes: Route = GET(getState)
}

object SubastaStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withSujeto: (String => Route) => Route = nestedRoute("sujeto") _
  def withObjeto: (String => Route) => Route = nestedRoute("objeto") _
  def withTipoObjeto: (String => Route) => Route = nestedRoute("tipo") _
  def withSubasta = path("subasta" / Segment)

}
