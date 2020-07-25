package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoUpdatedFromObligacion
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler

import scala.util.{Success, Try}

class ObjetoUpdateFromObligacionHandler(actor: ObjetoActor)
    extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromObligacion] {
  override def handle(
      command: ObjetoCommands.ObjetoUpdateFromObligacion
  ): Try[akka.Done] = {
    val event = ObjetoUpdatedFromObligacion(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.obligacionId,
      command.obligacionVencida,
      command.saldoObligacion,
      command.obligacionExenta,
      command.porcentajeExencion
    )
    actor.persistEvent(event) { () =>
      actor.state += event
      actor.informParent(command, actor.state)
      actor.persistSnapshot(event, actor.state)
    }
    Success(akka.Done)
  }
}
