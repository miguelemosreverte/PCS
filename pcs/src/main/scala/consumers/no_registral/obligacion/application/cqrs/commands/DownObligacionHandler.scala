package consumers.no_registral.obligacion.application.cqrs.commands

import akka.Done
import design_principles.actor_model.mechanism.DeliveryIdManagement._
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.DownObligacion
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor.ObligacionTags
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class DownObligacionHandler(actor: ObligacionActor) extends SyncCommandHandler[DownObligacion] {
  override def handle(command: DownObligacion): Try[Response.SuccessProcessing] = {
    val event =
      ObligacionEvents.ObligacionRemoved(
        command.deliveryId,
        command.sujetoId,
        command.objetoId,
        command.tipoObjeto,
        command.obligacionId
      )
    if (validateCommand(event, command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(s"[${actor.persistenceId}] respond idempotent because of old delivery id | $command")
      actor.context.sender() ! Response.SuccessProcessing(command.deliveryId)
    } else {
      actor.persistEvent(event, ObligacionTags.ObligacionReadside) { () =>
        actor.state += event
        actor.informBajaToParent(command)
        actor.context.sender() ! Response.SuccessProcessing(command.deliveryId)
        // actor.context.stop(actor.self)
      }
    }
    Success(Response.SuccessProcessing(command.deliveryId))
  }
}
