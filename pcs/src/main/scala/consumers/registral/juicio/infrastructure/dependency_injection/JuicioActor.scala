package consumers.registral.juicio.infrastructure.dependency_injection

import akka.actor.typed.ActorSystem
import com.typesafe.config.Config
import consumers.registral.juicio.application.cqrs.commands.JuicioUpdateFromDtoHandler
import consumers.registral.juicio.application.cqrs.queries.GetStateJuicioHandler
import consumers.registral.juicio.application.entities.JuicioCommands.JuicioUpdateFromDto
import consumers.registral.juicio.application.entities.JuicioMessage
import consumers.registral.juicio.application.entities.JuicioQueries.GetStateJuicio
import consumers.registral.juicio.domain.JuicioEvents.JuicioUpdatedFromDto
import consumers.registral.juicio.domain.events.JuicioUpdatedFromDtoHandler
import consumers.registral.juicio.domain.{JuicioEvents, JuicioState}
import cqrs.base_actor.typed.BasePersistentShardedTypedActorWithCQRS

case class JuicioActor(state: JuicioState = JuicioState(), config: Config)(
    implicit system: ActorSystem[Nothing]
) extends BasePersistentShardedTypedActorWithCQRS[
      JuicioMessage,
      JuicioEvents,
      JuicioState
    ](state, config) {

  override def tags(event: JuicioEvents): Set[String] = event match {
    case _: JuicioUpdatedFromDto => Set("Juicio")
  }

  commandBus.subscribe[JuicioUpdateFromDto](new JuicioUpdateFromDtoHandler().handle)
  queryBus.subscribe[GetStateJuicio](new GetStateJuicioHandler().handle)
  eventBus.subscribe[JuicioUpdatedFromDto](new JuicioUpdatedFromDtoHandler().handle)
}
