package consumers.no_registral.obligacion.application.cqrs.commands

import consumers.no_registral.obligacion.application.entities.ObligacionCommands
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor

import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObligacionUpdateExencionHandler(actor: ObligacionActor)
    extends SyncCommandHandler[ObligacionCommands.ObligacionUpdateExencion] {
  override def handle(
      command: ObligacionCommands.ObligacionUpdateExencion
  ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    val receivesExencion = (for {
      fechaInicio <- command.exencion.BEX_FECHA_INICIO
      fechaFin <- command.exencion.BEX_FECHA_FIN
      objetoDocument <- actor.state.registro
      fechaVencimiento <- objetoDocument.BOB_VENCIMIENTO
      receivesExencion = (
        fechaInicio.compareTo(fechaVencimiento) <= 0
        && fechaFin.compareTo(fechaVencimiento) >= 0
      )
    } yield receivesExencion).getOrElse(false)

    if (receivesExencion) {
      val event = ObligacionEvents.ObligacionAddedExencion(
        command.deliveryId,
        command.sujetoId,
        command.objetoId,
        command.tipoObjeto,
        command.obligacionId,
        command.exencion
      )
      actor.persistEvent(event) { () =>
        actor.state += event
        actor.persistSnapshot() { () =>
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))

  }
}
