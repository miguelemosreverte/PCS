package consumers.no_registral.obligacion.application.cqrs.commands

import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionUpdateFromDto
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionUpdatedFromDto
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor

import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObligacionUpdateFromDtoHandler(actor: ObligacionActor) extends SyncCommandHandler[ObligacionUpdateFromDto] {
  override def handle(command: ObligacionUpdateFromDto): Try[Response.SuccessProcessing] = {
    val replyTo = actor.context.sender()
    if (command.deliveryId <= actor.lastDeliveryId) {
      log.warn(s"[${actor.persistenceId}] respond idempotent because of old delivery id | $command")
      replyTo ! Success(Response.SuccessProcessing())
      Success(Response.SuccessProcessing())
    } else {
      val event =
        ObligacionUpdatedFromDto(command.sujetoId,
                                 command.objetoId,
                                 command.tipoObjeto,
                                 command.obligacionId,
                                 command.registro,
                                 command.detallesObligacion)
      actor.persistEvent(event) { () =>
        actor.state += event
        actor.lastDeliveryId = command.deliveryId
        actor.informParent(command)
        actor.persistSnapshot() { () =>
          actor.context.sender() ! Success(Response.SuccessProcessing())
        }
      }
      Success(Response.SuccessProcessing())
    }
  }
}
