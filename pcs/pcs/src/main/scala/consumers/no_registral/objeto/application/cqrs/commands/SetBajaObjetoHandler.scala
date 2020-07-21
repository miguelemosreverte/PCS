package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler

import scala.util.{Success, Try}

class SetBajaObjetoHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.SetBajaObjeto] {
  override def handle(
      command: ObjetoCommands.SetBajaObjeto
  ): Try[akka.Done] = {
    val replyTo = actor.context.sender()
    val event = ObjetoEvents.ObjetoBajaSet(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.registro,
      command.isResponsable,
      command.sujetoResponsable
    )
    val documentName = utils.Inference.getSimpleName(event.getClass.getName)
    val lastDeliveryId = actor.state.lastDeliveryIdByEvents.getOrElse(documentName, BigInt(0))
    if (event.deliveryId <= lastDeliveryId) {
      replyTo ! akka.Done
    } else {
      actor.persistEvent(event) { () =>
        actor.state += event
        actor.informBajaToParent(command)
        actor.persistSnapshot(event, actor.state)
        replyTo ! akka.Done
      }
    }
    Success(akka.Done)
  }
}