package consumers.no_registral.obligacion.infrastructure.http

import java.time.LocalDateTime

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotFound, OK}
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import api.Utils
import components.QueryStateAPI
import consumers.no_registral.objeto.application.entities.ObjetoQueries.GetStateObjeto
import consumers.no_registral.objeto.application.entities.ObjetoResponses.GetObjetoResponse
import consumers.no_registral.obligacion.application.entities.ObligacionQueries.GetStateObligacion
import consumers.no_registral.obligacion.application.entities.ObligacionResponses.GetObligacionResponse
import consumers.no_registral.obligacion.infrastructure.json._

case class ObligacionStateAPI()(implicit actor: ActorRef, system: ActorSystem) extends QueryStateAPI {
  import ObligacionStateAPI._
  implicit def ec = system.dispatcher

  // TODO Create DeveloperToolsAPI
  def developerTools: Route =
    withSujeto { sujetoId =>
      withObjeto { objetoId =>
        withTipoObjeto { tipoObjeto =>
          withObligacion { obligacionId =>
            path("developer" / "tools" / Segment) { command =>
              command match {
                case "sleep" =>
                  log.info(
                    s"[Sujeto-$sujetoId-Objeto-$objetoId-$tipoObjeto-Obligacion-$obligacionId] ${Console.RED} Going to sleep. ${Console.RESET}"
                  )
                  actor ! PoisonPill
              }
              complete { HttpResponse(OK) }
            }
          }
        }
      }
    }

  def getState: Route =
    withSujeto { sujetoId =>
      withObjeto { objetoId =>
        withTipoObjeto { tipoObjeto =>
          path("obligacion" / Segment) { obligacionId =>
            complete {
              (for {
                objetoState <- actor.ask[GetObjetoResponse](GetStateObjeto(sujetoId, objetoId, tipoObjeto))

                sujetoResponsable = objetoState.sujetoResponsable match {
                  case Some(value) => value
                  case None => sujetoId
                }

                obligacionState <- actor.ask[GetObligacionResponse](
                  GetStateObligacion(sujetoResponsable, objetoId, tipoObjeto, obligacionId)
                )
              } yield {
                obligacionState match {
                  case _: GetObligacionResponse if obligacionState.fechaUltMod == LocalDateTime.MIN =>
                    HttpResponse(NotFound)

                  case result: GetObligacionResponse =>
                    HttpResponse(
                      OK,
                      entity = Utils.standarization(
                        serialization.encode(result)(GetObligacionResponseF)
                      )
                    )
                }
              }).recover { case e: Exception => HttpResponse(InternalServerError, entity = e.getMessage) }
            }

          }
        }
      }
    }

  def routes: Route = GET(getState) ~ POST(developerTools)

}
object ObligacionStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withSujeto: (String => Route) => Route = nestedRoute("sujeto") _
  def withObjeto: (String => Route) => Route = nestedRoute("objeto") _
  def withTipoObjeto: (String => Route) => Route = nestedRoute("tipo") _
  def withObligacion: (String => Route) => Route = nestedRoute("obligacion") _

}
