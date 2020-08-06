package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObjetoTagAddHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.ObjetoTagAdd] {
  override def handle(
      command: ObjetoCommands.ObjetoTagAdd
  ): Try[Response.SuccessProcessing] = {
    val replyTo = actor.sender()
    val event = ObjetoEvents.ObjetoTagAdded(command.deliveryId,
                                            command.sujetoId,
                                            command.objetoId,
                                            command.tipoObjeto,
                                            command.tag)
    actor.persistEvent(event) { () =>
      actor.state += event
      actor.persistSnapshot(event, actor.state)
      replyTo ! Success(Response.SuccessProcessing())
    }
    Success(Response.SuccessProcessing())
  }
}
