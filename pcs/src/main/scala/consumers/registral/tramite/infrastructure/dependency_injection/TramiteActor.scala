package consumers.registral.tramite.infrastructure.dependency_injection

import akka.actor.typed.ActorSystem
import consumers.registral.tramite.application.cqrs.commands.TramiteUpdateFromDtoHandler
import consumers.registral.tramite.application.cqrs.queries.GetStateTramiteHandler
import consumers.registral.tramite.application.entities.TramiteCommands.TramiteUpdateFromDto
import consumers.registral.tramite.application.entities.TramiteMessage
import consumers.registral.tramite.application.entities.TramiteQueries.GetStateTramite
import consumers.registral.tramite.domain.TramiteEvents.TramiteUpdatedFromDto
import consumers.registral.tramite.domain.events.TramiteUpdatedFromDtoHandler
import consumers.registral.tramite.domain.{TramiteEvents, TramiteState}
import cqrs.BasePersistentShardedTypedActor.CQRS.BasePersistentShardedTypedActorWithCQRS

case class TramiteActor(state: TramiteState = TramiteState())(
    implicit system: ActorSystem[Nothing]
) extends BasePersistentShardedTypedActorWithCQRS[
      TramiteMessage,
      TramiteEvents,
      TramiteState
    ](state) {

  override def tags(event: TramiteEvents): Set[String] = event match {
    case _: TramiteUpdatedFromDto => Set("Tramite")
  }

  commandBus.subscribe[TramiteUpdateFromDto](new TramiteUpdateFromDtoHandler().handle)
  queryBus.subscribe[GetStateTramite](new GetStateTramiteHandler().handle)
  eventBus.subscribe[TramiteUpdatedFromDto](new TramiteUpdatedFromDtoHandler().handle)
}
