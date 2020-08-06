package consumers.no_registral.objeto.application.cqrs.commands

import scala.util.{Success, Try}
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.obligacion.application.entities.ObligacionCommands
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

class ObjetoAddExencionHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.ObjetoAddExencion] {
  override def handle(
      command: ObjetoCommands.ObjetoAddExencion
  ): Try[Response.SuccessProcessing] = {
    val replyTo = actor.sender()
    val event = ObjetoEvents.ObjetoAddedExencion(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.exencion
    )

    val documentName = utils.Inference.getSimpleName(event.getClass.getName)
    val lastDeliveryId = actor.state.lastDeliveryIdByEvents.getOrElse(documentName, BigInt(0))
    if (event.deliveryId <= lastDeliveryId) {
      log.warn(s"[${actor.persistenceId}] respond idempotent because of old delivery id | $command")
      replyTo ! Success(Response.SuccessProcessing(command.deliveryId))
    } else {
      actor.persistEvent(event, Set("Exencion")) { () =>
        actor.state += event
        actor.state.obligaciones.foreach { obligacionId =>
          val obligacion = actor.obligaciones((command.sujetoId, command.objetoId, command.tipoObjeto, obligacionId))
          obligacion ! ObligacionCommands.ObligacionUpdateExencion(command.deliveryId,
                                                                   command.sujetoId,
                                                                   command.objetoId,
                                                                   command.tipoObjeto,
                                                                   obligacionId.split("-").last,
                                                                   command.exencion)
        }
        actor.informParent(command, actor.state)
        replyTo ! Success(Response.SuccessProcessing(command.deliveryId))
      }
    }
    Success(Response.SuccessProcessing(command.deliveryId))
  }

}
