package consumers.no_registral.obligacion.application.cqrs.commands

import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionUpdateFromDto
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionUpdatedFromDto
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import consumers.no_registral.obligacion.infrastructure.event_bus.ObligacionPersistedSnapshotHandler
import cqrs.untyped.command.CommandHandler.SyncCommandHandler

import scala.util.{Success, Try}

class ObligacionUpdateFromDtoHandler(actor: ObligacionActor) extends SyncCommandHandler[ObligacionUpdateFromDto] {
  override def handle(command: ObligacionUpdateFromDto): Try[akka.Done] = {
    val replyTo = actor.context.sender()
    if (command.deliveryId < actor.lastDeliveryId) {
      log.info(s"[${actor.persistenceId}] Will have to answer idepotent for delivery ${command.deliveryId}")
      replyTo ! akka.Done
      Success(akka.Done)
    } else {
      val event =
        ObligacionUpdatedFromDto(command.sujetoId,
                                 command.objetoId,
                                 command.tipoObjeto,
                                 command.obligacionId,
                                 command.registro,
                                 command.detallesObligacion)
      actor.persistEvent(event) { () =>
        actor.state += event
        actor.lastDeliveryId = command.deliveryId
        actor.informParent(command)
        replyTo ! akka.Done
        actor.eventBus.publish(ObligacionPersistedSnapshotHandler.toEvent(command, actor))
      }
      Success(akka.Done)
    }
  }
}
