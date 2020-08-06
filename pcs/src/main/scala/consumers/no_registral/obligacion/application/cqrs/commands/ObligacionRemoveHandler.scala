package consumers.no_registral.obligacion.application.cqrs.commands

import akka.Done
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionRemove
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor.ObligacionTags
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObligacionRemoveHandler(actor: ObligacionActor) extends SyncCommandHandler[ObligacionRemove] {
  override def handle(command: ObligacionRemove): Try[Response.SuccessProcessing] = {
    val replyTo = actor.context.sender()
    val event =
      ObligacionEvents.ObligacionRemoved(
        command.sujetoId,
        command.objetoId,
        command.tipoObjeto,
        command.obligacionId
      )
    actor.persistEvent(event, ObligacionTags.ObligacionReadside) { () =>
      actor.state += event
      replyTo ! Success(Response.SuccessProcessing(command.deliveryId))
    }
    Success(Response.SuccessProcessing(command.deliveryId))
  }
}
