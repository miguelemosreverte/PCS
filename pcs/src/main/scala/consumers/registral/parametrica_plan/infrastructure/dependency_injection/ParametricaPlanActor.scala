package consumers.registral.parametrica_plan.infrastructure.dependency_injection

import akka.actor.typed.ActorSystem
import consumers.registral.parametrica_plan.application.cqrs.commands.ParametricaPlanUpdateFromDtoHandler
import consumers.registral.parametrica_plan.application.cqrs.queries.GetStateParametricaPlanHandler
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanCommands.ParametricaPlanUpdateFromDto
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanMessage
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanQueries.GetStateParametricaPlan
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents.ParametricaPlanUpdatedFromDto
import consumers.registral.parametrica_plan.domain.events.ParametricaPlanUpdatedFromDtoHandler
import consumers.registral.parametrica_plan.domain.{ParametricaPlanEvents, ParametricaPlanState}
import cqrs.BasePersistentShardedTypedActor.CQRS.BasePersistentShardedTypedActorWithCQRS

case class ParametricaPlanActor(state: ParametricaPlanState = ParametricaPlanState())(
    implicit system: ActorSystem[Nothing]
) extends BasePersistentShardedTypedActorWithCQRS[
      ParametricaPlanMessage,
      ParametricaPlanEvents,
      ParametricaPlanState
    ](state) {

  override def tags(event: ParametricaPlanEvents): Set[String] = event match {
    case _: ParametricaPlanUpdatedFromDto => Set("ParametricaPlan")
  }

  commandBus.subscribe[ParametricaPlanUpdateFromDto](new ParametricaPlanUpdateFromDtoHandler().handle)
  queryBus.subscribe[GetStateParametricaPlan](new GetStateParametricaPlanHandler().handle)
  eventBus.subscribe[ParametricaPlanUpdatedFromDto](new ParametricaPlanUpdatedFromDtoHandler().handle)
}
