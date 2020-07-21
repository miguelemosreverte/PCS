package consumers.no_registral.sujeto.application.cqrs.commands

import akka.Done
import consumers.no_registral.sujeto.application.entity.SujetoCommands.SujetoUpdateFromAnt
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoUpdatedFromAnt
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler

import scala.util.{Success, Try}

class SujetoUpdateFromAntHandler(actor: SujetoActor) extends SyncCommandHandler[SujetoUpdateFromAnt] {
  override def handle(command: SujetoUpdateFromAnt): Try[Done] = {
    val replyTo = actor.context.sender()
    val event = SujetoUpdatedFromAnt(command.deliveryId, command.sujetoId, command.registro)
    val documentName = utils.Inference.getSimpleName(event.getClass.getName)
    val lastDeliveryId = actor.state.lastDeliveryIdByEvents.getOrElse(documentName, BigInt(0))

    if (command.deliveryId <= lastDeliveryId) {
      log.info(s"[${actor.persistenceId}] respond idempotent because of old delivery id | $command")
      replyTo ! akka.Done
    } else {
      actor.persistEvent(event) { () =>
        actor.state += event
        actor.persistSnapshot()
        replyTo ! akka.Done
      }
    }
    Success(akka.Done)
  }

}
