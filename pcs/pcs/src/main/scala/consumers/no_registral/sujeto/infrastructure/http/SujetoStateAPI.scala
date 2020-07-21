package consumers.no_registral.sujeto.infrastructure.http

import java.time.LocalDateTime

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import components.QueryStateAPI
import consumers.no_registral.obligacion.infrastructure.http.ObligacionStateAPI.withSujeto
import consumers.no_registral.sujeto.application.entity.SujetoQueries.GetStateSujeto
import consumers.no_registral.sujeto.application.entity.SujetoResponses.GetSujetoResponse
import consumers.no_registral.sujeto.infrastructure.json._
case class SujetoStateAPI()(implicit actor: ActorRef, system: ActorSystem) extends QueryStateAPI {
  def developerTools: Route =
    withSujeto { sujetoId =>
      withDeveloperTools { command =>
        command match {
          case "sleep" =>
            log.info(s"[Sujeto-$sujetoId] ${Console.RED} Going to sleep. ${Console.RESET}")
            actor ! PoisonPill
        }
        complete { HttpResponse(OK) }
      }
    }

  def getState: Route =
    path("sujeto" / Segment) { sujetoId =>
      queryState[GetSujetoResponse](actor, GetStateSujeto(sujetoId))(
        GetSujetoResponseF,
        state => state.fechaUltMod == LocalDateTime.MIN
      )
    }
  def routes: Route = GET(getState) ~ GET(developerTools)
  def withDeveloperTools = path("developer" / "tools" / Segment)
}
