package consumers.no_registral.sujeto.application.cqrs.commands

import design_principles.actor_model.mechanism.DeliveryIdManagement._

import akka.Done
import consumers.no_registral.sujeto.application.entity.SujetoCommands.SujetoUpdateFromTri
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoUpdatedFromTri
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class SujetoUpdateFromTriHandler(actor: SujetoActor) extends SyncCommandHandler[SujetoUpdateFromTri] {
  override def handle(command: SujetoUpdateFromTri): Try[Response.SuccessProcessing] = {

    val event = SujetoUpdatedFromTri(command.deliveryId, command.sujetoId, command.registro)

    if (validateCommand(event, command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(s"[${actor.persistenceId}] respond idempotent because of old delivery id | $command")
      actor.context.sender() ! Response.SuccessProcessing(command.deliveryId)
    } else {
      println("       about to call SujetoUpdateFromTriHandler.actor.persistEvent")

      actor.persistEvent(event) { () =>
        actor.state += event
        println("        SujetoUpdateFromTriHandler.actor.persistEvent() { () =>")
        actor.persistSnapshot() { () =>
          println("        SujetoUpdateFromTriHandler.actor.persistSnapshot() { () =>")
          actor.context.sender() ! Response.SuccessProcessing(command.deliveryId)
        }
      }
    }
    Success(Response.SuccessProcessing(command.deliveryId))
  }
}
