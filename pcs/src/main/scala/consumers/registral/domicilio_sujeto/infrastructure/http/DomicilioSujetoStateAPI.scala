package consumers.registral.domicilio_sujeto.infrastructure.http

import java.time.LocalDateTime

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.{Directive, Route}
import components.QueryStateAPI
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoQueries.GetStateDomicilioSujeto
import consumers.registral.domicilio_sujeto.infrastructure.dependency_injection.DomicilioSujetoActor
import consumers.registral.domicilio_sujeto.infrastructure.json._

case class DomicilioSujetoStateAPI()(implicit actor: DomicilioSujetoActor, system: akka.actor.typed.ActorSystem[_])
    extends QueryStateAPI {
  import DomicilioSujetoStateAPI._
  def getState: Route =
    withSujeto { sujetoId =>
      withDomicilioSujeto { domicilioId =>
        queryState(actor, GetStateDomicilioSujeto(sujetoId, domicilioId))(
          GetDomicilioSujetoResponseF,
          state => state.fechaUltMod == LocalDateTime.MIN
        )
      }
    }

  def routes: Route = GET(getState)
}

object DomicilioSujetoStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withSujeto: (String => Route) => Route = nestedRoute("sujeto") _
  def withDomicilioSujeto: Directive[Tuple1[String]] = path("domicilio_sujeto" / Segment)
}
