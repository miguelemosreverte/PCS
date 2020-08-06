package consumers.no_registral.obligacion.infrastructure.event_bus

import scala.util.{Success, Try}
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionPersistedSnapshot
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor.ObligacionTags
import cqrs.untyped.event.EventHandler.SyncEventHandler
import design_principles.actor_model.Response

class ObligacionRemovedHandler(actor: ObligacionActor) extends SyncEventHandler[ObligacionPersistedSnapshot] {
  override def handle(
      event: ObligacionPersistedSnapshot
  ): Try[Unit] = {

    actor.persistEvent(event, ObligacionTags.ObligacionReadside) { () =>
      ()
    }
    Success(Response.SuccessProcessing())
  }
}
