package consumers.registral.plan_pago.infrastructure.http

import java.time.LocalDateTime

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import components.QueryStateAPI
import consumers.registral.plan_pago.application.entities.PlanPagoQueries.GetStatePlanPago
import consumers.registral.plan_pago.infrastructure.dependency_injection.PlanPagoActor
import consumers.registral.plan_pago.infrastructure.json._

case class PlanPagoStateAPI()(implicit actor: PlanPagoActor, system: akka.actor.typed.ActorSystem[_])
    extends QueryStateAPI {
  import PlanPagoStateAPI._

  def getState: Route =
    withSujeto { sujetoId =>
      withObjeto { objetoId =>
        withTipoObjeto { tipoObjeto =>
          withPlanPago { planPagoId =>
            queryState(actor, GetStatePlanPago(sujetoId, objetoId, tipoObjeto, planPagoId))(
              GetPlanPagoResponseF,
              state => state.fechaUltMod == LocalDateTime.MIN
            )
          }
        }
      }
    }

  def routes: Route = GET(getState)
}

object PlanPagoStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withSujeto: (String => Route) => Route = nestedRoute("sujeto") _
  def withObjeto: (String => Route) => Route = nestedRoute("objeto") _
  def withTipoObjeto: (String => Route) => Route = nestedRoute("tipo") _
  def withPlanPago = path("plan_pago" / Segment)

}
