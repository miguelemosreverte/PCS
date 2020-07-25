package consumers.no_registral.obligacion.infrastructure.event_bus

import scala.util.{Success, Try}

import consumers.no_registral.obligacion.application.entities.ObligacionCommands
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionPersistedSnapshot
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor.ObligacionTags
import cqrs.untyped.event.EventHandler.SyncEventHandler

class ObligacionPersistedSnapshotHandler(actor: ObligacionActor) extends SyncEventHandler[ObligacionPersistedSnapshot] {
  override def handle(
      event: ObligacionPersistedSnapshot
  ): Try[Unit] = {

    actor.persistEvent(event, ObligacionTags.ObligacionReadside) { () =>
      ()
    }
    Success(akka.Done)
  }

}

object ObligacionPersistedSnapshotHandler {
  def toEvent(cmd: ObligacionCommands, actor: ObligacionActor): ObligacionPersistedSnapshot =
    ObligacionPersistedSnapshot(
      cmd.sujetoId,
      cmd.objetoId,
      cmd.tipoObjeto,
      cmd.obligacionId,
      actor.state.registro,
      actor.state.exenta,
      actor.state.porcentajeExencion.getOrElse(0),
      actor.state.vencida,
      actor.state.saldo
    )

}
