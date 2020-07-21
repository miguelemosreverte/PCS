package consumers.no_registral.obligacion.application.cqrs.commands

import consumers.no_registral.obligacion.application.entities.ObligacionCommands
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import consumers.no_registral.obligacion.infrastructure.event_bus.ObligacionPersistedSnapshotHandler
import cqrs.untyped.command.CommandHandler.SyncCommandHandler

import scala.util.{Success, Try}

class ObligacionUpdateExencionHandler(actor: ObligacionActor)
    extends SyncCommandHandler[ObligacionCommands.ObligacionUpdateExencion] {
  override def handle(
      command: ObligacionCommands.ObligacionUpdateExencion
  ): Try[akka.Done] = {
    val receivesExencion = (for {
      fechaInicio <- command.exencion.BEX_FECHA_INICIO
      fechaFin <- command.exencion.BEX_FECHA_FIN
      objetoDocument <- actor.state.registro
      fechaVencimiento <- objetoDocument.BOB_VENCIMIENTO
      receivesExencion = (
        fechaInicio.compareTo(fechaVencimiento) <= 0
        && fechaFin.compareTo(fechaVencimiento) >= 0
      )
    } yield {
      log.info(s"""[${actor.persistenceId}] Analysing exencion under the following condition:
                     |                        fechaInicioExencion        < fechaVencimientoObligacion        < fechaFinExencion
                     |                        $fechaInicio   <  $fechaVencimiento           < $fechaFin
                     |                                          $receivesExencion
                     |
                     |                        """.stripMargin)
      receivesExencion
    }).getOrElse(false)

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
        actor.state += event // use eventBus
        actor.eventBus.publish(ObligacionPersistedSnapshotHandler.toEvent(command, actor))

      }
    }
    Success(akka.Done)

  }
}
