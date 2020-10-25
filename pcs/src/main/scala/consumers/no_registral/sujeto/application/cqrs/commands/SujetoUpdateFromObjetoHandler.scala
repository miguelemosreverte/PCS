package consumers.no_registral.sujeto.application.cqrs.commands

import akka.Done
import consumers.no_registral.sujeto.application.entity.SujetoCommands.SujetoUpdateFromObjeto
import consumers.no_registral.sujeto.domain.SujetoEvents
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class SujetoUpdateFromObjetoHandler(actor: SujetoActor) extends SyncCommandHandler[SujetoUpdateFromObjeto] {
  override def handle(command: SujetoUpdateFromObjeto): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    val event = SujetoEvents.SujetoUpdatedFromObjeto(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.saldoObjeto,
      command.saldoObligaciones
    )

    actor.persistEvent(event) { () =>
      actor.state += event
      println(s"HERE 4: sujeto received update: ${command.aggregateRoot} ${command.saldoObjeto}")
      println(s"HERE 5: sujeto received update: ${actor.state.saldo}")
      log.info(s"[${actor.persistenceId}] GetState | ${actor.state}")

      actor.persistSnapshot()(_ => ())
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
