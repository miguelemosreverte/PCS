package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor.ObjetoTags
import cqrs.untyped.command.CommandHandler.SyncCommandHandler

import scala.util.{Success, Try}

class ObjetoUpdateFromTriHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromTri] {
  override def handle(
      command: ObjetoCommands.ObjetoUpdateFromTri
  ): Try[akka.Done] = {
    val replyTo = actor.context.sender()
    val event = ObjetoEvents.ObjetoUpdatedFromTri(
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
    if (event.deliveryId < lastDeliveryId) {
      replyTo ! akka.Done
    } else {
      // because ObjetoNovedadCotitularidad, the event processor, needs this event to publish AddCotitular
      actor.persistEvent(event, ObjetoTags.CotitularesReadside) { () =>
        actor.state += event
        actor.informParent(command, actor.state)
        actor.persistSnapshot(event, actor.state)
        if (!actor.state.isResponsable)
          actor.removeObligaciones()
        replyTo ! akka.Done
      }
    }
    Success(akka.Done)
  }
}
