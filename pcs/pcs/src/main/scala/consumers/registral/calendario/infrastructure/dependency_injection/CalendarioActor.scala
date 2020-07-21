package consumers.registral.calendario.infrastructure.dependency_injection

import akka.actor.typed.ActorSystem
import consumers.registral.calendario.application.cqrs.commands.CalendarioUpdateFromDtoHandler
import consumers.registral.calendario.application.cqrs.queries.GetStateCalendarioHandler
import consumers.registral.calendario.application.entities.CalendarioCommands.CalendarioUpdateFromDto
import consumers.registral.calendario.application.entities.CalendarioMessage
import consumers.registral.calendario.application.entities.CalendarioQueries.GetStateCalendario
import consumers.registral.calendario.domain.CalendarioEvents.CalendarioUpdatedFromDto
import consumers.registral.calendario.domain.events.CalendarioUpdatedFromDtoHandler
import consumers.registral.calendario.domain.{CalendarioEvents, CalendarioState}
import cqrs.BasePersistentShardedTypedActor.CQRS.BasePersistentShardedTypedActorWithCQRS

case class CalendarioActor(state: CalendarioState = CalendarioState())(
    implicit system: ActorSystem[Nothing]
) extends BasePersistentShardedTypedActorWithCQRS[
      CalendarioMessage,
      CalendarioEvents,
      CalendarioState
    ](state) {

  override def tags(event: CalendarioEvents): Set[String] = event match {
    case _: CalendarioUpdatedFromDto => Set("Calendario")
  }

  commandBus.subscribe[CalendarioUpdateFromDto](new CalendarioUpdateFromDtoHandler().handle)
  queryBus.subscribe[GetStateCalendario](new GetStateCalendarioHandler().handle)
  eventBus.subscribe[CalendarioUpdatedFromDto](new CalendarioUpdatedFromDtoHandler().handle)
}
