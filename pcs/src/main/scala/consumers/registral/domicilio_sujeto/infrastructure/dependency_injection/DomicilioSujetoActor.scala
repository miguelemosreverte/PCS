package consumers.registral.domicilio_sujeto.infrastructure.dependency_injection

import akka.actor.typed.ActorSystem
import consumers.registral.domicilio_sujeto.application.cqrs.commands.DomicilioSujetoUpdateFromDtoHandler
import consumers.registral.domicilio_sujeto.application.cqrs.queries.GetStateDomicilioSujetoHandler
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoCommands.DomicilioSujetoUpdateFromDto
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoMessage
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoQueries.GetStateDomicilioSujeto
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents.DomicilioSujetoUpdatedFromDto
import consumers.registral.domicilio_sujeto.domain.events.DomicilioSujetoUpdatedFromDtoHandler
import consumers.registral.domicilio_sujeto.domain.{DomicilioSujetoEvents, DomicilioSujetoState}
import cqrs.base_actor.typed.BasePersistentShardedTypedActorWithCQRS

case class DomicilioSujetoActor(state: DomicilioSujetoState = DomicilioSujetoState())(
    implicit system: ActorSystem[Nothing]
) extends BasePersistentShardedTypedActorWithCQRS[
      DomicilioSujetoMessage,
      DomicilioSujetoEvents,
      DomicilioSujetoState
    ](state) {

  override def tags(event: DomicilioSujetoEvents): Set[String] = event match {
    case _: DomicilioSujetoUpdatedFromDto => Set("DomicilioSujeto")
  }

  commandBus.subscribe[DomicilioSujetoUpdateFromDto](new DomicilioSujetoUpdateFromDtoHandler().handle)
  queryBus.subscribe[GetStateDomicilioSujeto](new GetStateDomicilioSujetoHandler().handle)
  eventBus.subscribe[DomicilioSujetoUpdatedFromDto](new DomicilioSujetoUpdatedFromDtoHandler().handle)
}
