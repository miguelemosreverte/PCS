package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoUpdatedFromObligacionBajaSet
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler

import scala.util.{Success, Try}

class ObjetoUpdateFromSetBajaObligacionHandler(actor: ObjetoActor)
    extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromSetBajaObligacion] {
  override def handle(command: ObjetoCommands.ObjetoUpdateFromSetBajaObligacion): Try[akka.Done] = {
    val event = ObjetoUpdatedFromObligacionBajaSet(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.obligacionId
    )
    actor.persistEvent(event) { () =>
      actor.state += event
      actor.persistSnapshot(event, actor.state)
    }
    Success(akka.Done)
  }
}
