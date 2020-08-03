package consumers.no_registral.sujeto.application.cqrs.commands

import akka.Done
import consumers.no_registral.sujeto.application.entity.SujetoCommands.SujetoUpdateFromTri
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoUpdatedFromTri
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler

import scala.util.{Success, Try}

class SujetoUpdateFromTriHandler(actor: SujetoActor) extends SyncCommandHandler[SujetoUpdateFromTri] {
  override def handle(command: SujetoUpdateFromTri): Try[Done] = {
    val replyTo = actor.context.sender()
    val event = SujetoUpdatedFromTri(command.deliveryId, command.sujetoId, command.registro)
    val documentName = utils.Inference.getSimpleName(event.getClass.getName)
    val lastDeliveryId = actor.state.lastDeliveryIdByEvents.getOrElse(documentName, BigInt(0))

    if (command.deliveryId <= lastDeliveryId) {
      log.warn(s"[${actor.persistenceId}] respond idempotent because of old delivery id | $command")
      replyTo ! akka.Done
    } else {
      actor.persistEvent(event) { () =>
        println("[SUJETO ACTOR] HERE THE MESSAGE ARRIVES: " + command)
        actor.state += event
        actor.persistSnapshot()
        replyTo ! akka.Done
      }
    }
    Success(akka.Done)
  }
}
