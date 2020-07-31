package consumers.no_registral.sujeto.application.cqrs.commands

import akka.Done
import consumers.no_registral.sujeto.application.entity.SujetoCommands.SujetoSetBajaFromObjeto
import consumers.no_registral.sujeto.domain.SujetoEvents
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler

import scala.util.{Success, Try}

class SujetoSetBajaFromObjetoHandler(actor: SujetoActor) extends SyncCommandHandler[SujetoSetBajaFromObjeto] {
  override def handle(command: SujetoSetBajaFromObjeto): Try[Done] = {
    val event = SujetoEvents.SujetoBajaFromObjetoSet(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto
    )

    actor.persistEvent(event) { () =>
      actor.state += event
      actor.persistSnapshot()()
    }
    Success(akka.Done)
  }
}
