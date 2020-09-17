package consumers.no_registral.sujeto.application.cqrs.commands

import akka.Done
import design_principles.actor_model.mechanism.DeliveryIdManagement._
import consumers.no_registral.sujeto.application.entity.SujetoCommands.SujetoUpdateFromAnt
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoUpdatedFromAnt
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class SujetoUpdateFromAntHandler(actor: SujetoActor) extends SyncCommandHandler[SujetoUpdateFromAnt] {
  override def handle(command: SujetoUpdateFromAnt): Try[Response.SuccessProcessing] = {

    val event = SujetoUpdatedFromAnt(command.deliveryId, command.sujetoId, command.registro)

    if (validateCommand(event, command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(s"[${actor.persistenceId}] respond idempotent because of old delivery id | $command")
      actor.context.sender() ! Response.SuccessProcessing(command.deliveryId)
    } else {
      actor.persistEvent(event) { () =>
        actor.state += event
        actor.persistSnapshot() { _ =>
          actor.context.sender() ! Response.SuccessProcessing(command.deliveryId)
        }
      }
    }
    Success(Response.SuccessProcessing(command.deliveryId))
  }

}
