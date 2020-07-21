package consumers.registral.subasta.infrastructure.dependency_injection

import akka.actor.typed.ActorSystem
import consumers.registral.subasta.application.cqrs.commands.SubastaUpdateFromDtoHandler
import consumers.registral.subasta.application.cqrs.queries.GetStateSubastaHandler
import consumers.registral.subasta.application.entities.SubastaCommands.SubastaUpdateFromDto
import consumers.registral.subasta.application.entities.SubastaMessage
import consumers.registral.subasta.application.entities.SubastaQueries.GetStateSubasta
import consumers.registral.subasta.domain.SubastaEvents.SubastaUpdatedFromDto
import consumers.registral.subasta.domain.events.SubastaUpdatedFromDtoHandler
import consumers.registral.subasta.domain.{SubastaEvents, SubastaState}
import cqrs.BasePersistentShardedTypedActor.CQRS.BasePersistentShardedTypedActorWithCQRS

case class SubastaActor(state: SubastaState = SubastaState())(
    implicit system: ActorSystem[Nothing]
) extends BasePersistentShardedTypedActorWithCQRS[
      SubastaMessage,
      SubastaEvents,
      SubastaState
    ](state) {

  override def tags(event: SubastaEvents): Set[String] = event match {
    case _: SubastaUpdatedFromDto => Set("Subasta")
  }

  commandBus.subscribe[SubastaUpdateFromDto](new SubastaUpdateFromDtoHandler().handle)
  queryBus.subscribe[GetStateSubasta](new GetStateSubastaHandler().handle)
  eventBus.subscribe[SubastaUpdatedFromDto](new SubastaUpdatedFromDtoHandler().handle)
}
