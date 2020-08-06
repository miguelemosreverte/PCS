package consumers.no_registral.obligacion.application.cqrs.commands

import akka.Done
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.DownObligacion
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor.ObligacionTags
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class DownObligacionHandler(actor: ObligacionActor) extends SyncCommandHandler[DownObligacion] {
  override def handle(command: DownObligacion): Try[Response.SuccessProcessing] = {
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
      actor.informBajaToParent(command)
      replyTo ! Success(Response.SuccessProcessing())
    }
    Success(Response.SuccessProcessing())
  }
}
