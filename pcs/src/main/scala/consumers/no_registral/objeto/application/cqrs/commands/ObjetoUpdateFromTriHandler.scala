package consumers.no_registral.objeto.application.cqrs.commands

import design_principles.actor_model.mechanism.DeliveryIdManagement._

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor.ObjetoTags
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObjetoUpdateFromTriHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromTri] {
  override def handle(
      command: ObjetoCommands.ObjetoUpdateFromTri
  ): Try[Response.SuccessProcessing] = {

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
      log.warn(s"[${actor.persistenceId}] respond idempotent because of old delivery id | $command")
      actor.context.sender() ! Response.SuccessProcessing(command.deliveryId)
    } else {
      // because ObjetoNovedadCotitularidad, the event processor, needs this event to publish AddCotitular
      actor.persistEvent(event, ObjetoTags.CotitularesReadside) { () =>
        actor.state += event
        actor.informParent(command, actor.state)
        actor.persistSnapshot(event, actor.state) { () =>
          if (!actor.state.isResponsable)
            actor.removeObligaciones()
          actor.context.sender() ! Response.SuccessProcessing(command.deliveryId)
        }
      }
    }
    Success(Response.SuccessProcessing(command.deliveryId))
  }
}
