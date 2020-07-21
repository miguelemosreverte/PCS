package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoUpdatedCotitulares
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler

import scala.util.{Success, Try}

class ObjetoUpdateCotitularesHandler(actor: ObjetoActor)
    extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateCotitulares] {
  override def handle(
      command: ObjetoCommands.ObjetoUpdateCotitulares
  ): Try[akka.Done] = {
    val replyTo = actor.sender()
    val event = ObjetoUpdatedCotitulares(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.cotitulares
    )
    actor.persistEvent(event) { () =>
      actor.state += event
      actor.informParent(command, actor.state)
      actor.persistSnapshot(event, actor.state)
      replyTo ! akka.Done
    }
    Success(akka.Done)
  }
}
