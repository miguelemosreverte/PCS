package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObjetoUpdateFromAntHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromAnt] {
  override def handle(
      command: ObjetoCommands.ObjetoUpdateFromAnt
  ): Try[Response.SuccessProcessing] = {

    val event = ObjetoEvents.ObjetoUpdatedFromAnt(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.registro
    )
    val documentName = utils.Inference.getSimpleName(event.getClass.getName)
    val lastDeliveryId = actor.state.lastDeliveryIdByEvents.getOrElse(documentName, BigInt(0))
    if (event.deliveryId <= lastDeliveryId) {
      log.warn(s"[${actor.persistenceId}] respond idempotent because of old delivery id | $command")
      actor.context.sender() ! Response.SuccessProcessing(command.deliveryId)
    } else {
      actor.persistEvent(event) { () =>
        actor.state += event
        actor.informParent(command, actor.state)
        actor.persistSnapshot(event, actor.state) { () =>
          actor.context.sender() ! Response.SuccessProcessing(command.deliveryId)
        }
      }
    }
    Success(Response.SuccessProcessing(command.deliveryId))
  }
}
