package consumers.no_registral.obligacion.infrastructure.dependency_injection

import akka.actor.Props
import akka.persistence._
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.obligacion.application.cqrs.commands._
import consumers.no_registral.obligacion.application.cqrs.queries.ObligacionGetStateHandler
import consumers.no_registral.obligacion.application.entities.{ObligacionCommands, ObligacionQueries}
import consumers.no_registral.obligacion.domain.{ObligacionEvents, ObligacionState}
import consumers.no_registral.obligacion.infrastructure.event_bus.ObligacionPersistedSnapshotHandler
import cqrs.PersistentBaseActor

class ObligacionActor() extends PersistentBaseActor[ObligacionEvents, ObligacionState] {

  var state = ObligacionState()

  override def receiveCommand: Receive = super.receiveCommand.orElse(oldReceiveCommand)
  def oldReceiveCommand: Receive = {
    case _: DeleteMessagesSuccess =>
      logger.info(s"[$persistenceId] Success to clean this actor akka.messages")

    case _: DeleteMessagesFailure =>
      logger.error(s"[$persistenceId] Failed to clean this actor akka.messages")

    case other => logger.error(s"[$persistenceId] Received UNEXPECTED message |  $other")

  }

  override def setupHandlers(): Unit = {
    queryBus.subscribe[ObligacionQueries.GetStateObligacion](new ObligacionGetStateHandler(this).handle)
    commandBus.subscribe[ObligacionCommands.ObligacionUpdateFromDto](new ObligacionUpdateFromDtoHandler(this).handle)
    commandBus.subscribe[ObligacionCommands.ObligacionUpdateExencion](new ObligacionUpdateExencionHandler(this).handle)
    commandBus.subscribe[ObligacionCommands.ObligacionRemove](new ObligacionRemoveHandler(this).handle)
    commandBus.subscribe[ObligacionCommands.DownObligacion](new DownObligacionHandler(this).handle)
    eventBus.subscribe[ObligacionEvents.ObligacionPersistedSnapshot](
      new ObligacionPersistedSnapshotHandler(this).handle
    )
  }

  def informParent(cmd: ObligacionCommands): Unit = {
    context.parent ! ObjetoCommands.ObjetoUpdateFromObligacion(
      cmd.deliveryId,
      cmd.sujetoId,
      cmd.objetoId,
      cmd.tipoObjeto,
      cmd.obligacionId,
      state.vencida,
      state.saldo,
      state.exenta,
      state.porcentajeExencion
    )
  }

  def informBajaToParent(cmd: ObligacionCommands): Unit = {
    context.parent ! ObjetoCommands.ObjetoUpdateFromSetBajaObligacion(
      cmd.deliveryId,
      cmd.sujetoId,
      cmd.objetoId,
      cmd.tipoObjeto,
      cmd.obligacionId
    )
  }

}

object ObligacionActor {
  def props: Props = Props(new ObligacionActor)

  object ObligacionTags {
    val ObligacionReadside: Set[String] = Set("Obligacion")
  }

}
