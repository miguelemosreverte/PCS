package consumers.registral.parametrica_plan.infrastructure.http

import java.time.LocalDateTime

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.{Directive, Route}
import components.QueryStateAPI
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanQueries.GetStateParametricaPlan
import consumers.registral.parametrica_plan.infrastructure.dependency_injection.ParametricaPlanActor
import consumers.registral.parametrica_plan.infrastructure.json._

case class ParametricaPlanStateAPI()(implicit actor: ParametricaPlanActor, system: akka.actor.typed.ActorSystem[_])
    extends QueryStateAPI {
  import ParametricaPlanStateAPI._

  def getState: Route =
    withParametricaPlan { parametricaPlanId =>
      queryState(actor, GetStateParametricaPlan(parametricaPlanId))(
        GetParametricaPlanResponseF,
        state => state.fechaUltMod == LocalDateTime.MIN
      )
    }

  def routes: Route = GET(getState)
}

object ParametricaPlanStateAPI {
  def nestedRoute(name: String)(andThen: String => Route): Route = pathPrefix(name / Segment)(andThen)
  def withParametricaPlan: Directive[Tuple1[String]] = path("parametrica_plan" / Segment)
}
