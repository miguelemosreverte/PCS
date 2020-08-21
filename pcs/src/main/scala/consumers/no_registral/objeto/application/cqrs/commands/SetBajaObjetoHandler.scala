package consumers.no_registral.objeto.application.cqrs.commands

import design_principles.actor_model.mechanism.DeliveryIdManagement._

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class SetBajaObjetoHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.SetBajaObjeto] {
  override def handle(
      command: ObjetoCommands.SetBajaObjeto
  ): Try[Response.SuccessProcessing] = {

    val event = ObjetoEvents.ObjetoBajaSet(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.registro,
      command.isResponsable,
      command.sujetoResponsable
    )
    if (validateCommand(event, command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(s"[${actor.persistenceId}] respond idempotent because of old delivery id | $command")
      actor.context.sender() ! Response.SuccessProcessing(command.deliveryId)
    } else {
      actor.persistEvent(event) { () =>
        actor.state += event
        actor.informBajaToParent(command)
        actor.persistSnapshot(event, actor.state) { () =>
          actor.context.sender() ! Response.SuccessProcessing(command.deliveryId)
        }
      }
    }
    Success(Response.SuccessProcessing(command.deliveryId))
  }
}
