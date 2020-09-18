package consumers.no_registral.objeto.application.cqrs.commands

import design_principles.actor_model.mechanism.DeliveryIdManagement._

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObjetoUpdateFromTriHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromTri] {
  override def handle(
      command: ObjetoCommands.ObjetoUpdateFromTri
  ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    val event = ObjetoEvents.ObjetoUpdatedFromTri(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.registro,
      command.isResponsable,
      command.sujetoResponsable
    )
    if (validateCommand(event, command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(
        s"[${actor.persistenceId}] respond idempotent because of old delivery id | $command -- last delivery id was: ${actor.state.lastDeliveryIdByEvents}"
      )
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
    } else {
      // because ObjetoNovedadCotitularidad, the event processor, needs this event to publish AddCotitular
      actor.persistEvent(event) { () =>
        actor.state += event
        actor.informParent(command, actor.state)
        log.warn(
          s"[${actor.persistenceId}] PERSISTING SNAPSHOT| $command -- last delivery id was: ${actor.state.lastDeliveryIdByEvents}"
        )

        actor.persistSnapshot(event, actor.state) { () =>
          if (!actor.state.isResponsable)
            actor.removeObligaciones()
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
