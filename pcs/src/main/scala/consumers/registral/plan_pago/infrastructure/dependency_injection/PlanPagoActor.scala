package consumers.registral.plan_pago.infrastructure.dependency_injection

import akka.actor.typed.ActorSystem
import consumers.registral.plan_pago.application.cqrs.commands.PlanPagoUpdateFromDtoHandler
import consumers.registral.plan_pago.application.cqrs.queries.GetStatePlanPagoHandler
import consumers.registral.plan_pago.application.entities.PlanPagoCommands.PlanPagoUpdateFromDto
import consumers.registral.plan_pago.application.entities.PlanPagoMessage
import consumers.registral.plan_pago.application.entities.PlanPagoQueries.GetStatePlanPago
import consumers.registral.plan_pago.domain.PlanPagoEvents.PlanPagoUpdatedFromDto
import consumers.registral.plan_pago.domain.events.PlanPagoUpdatedFromDtoHandler
import consumers.registral.plan_pago.domain.{PlanPagoEvents, PlanPagoState}
import cqrs.BasePersistentShardedTypedActor.CQRS.BasePersistentShardedTypedActorWithCQRS

case class PlanPagoActor(state: PlanPagoState = PlanPagoState())(
    implicit system: ActorSystem[Nothing]
) extends BasePersistentShardedTypedActorWithCQRS[
      PlanPagoMessage,
      PlanPagoEvents,
      PlanPagoState
    ](state) {

  override def tags(event: PlanPagoEvents): Set[String] = event match {
    case _: PlanPagoUpdatedFromDto => Set("PlanPago")
  }

  commandBus.subscribe[PlanPagoUpdateFromDto](new PlanPagoUpdateFromDtoHandler().handle)
  queryBus.subscribe[GetStatePlanPago](new GetStatePlanPagoHandler().handle)
  eventBus.subscribe[PlanPagoUpdatedFromDto](new PlanPagoUpdatedFromDtoHandler().handle)
}