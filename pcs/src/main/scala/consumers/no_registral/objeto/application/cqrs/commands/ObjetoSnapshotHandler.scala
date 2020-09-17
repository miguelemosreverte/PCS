package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObjetoSnapshotHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.ObjetoSnapshot] {
  override def handle(
      command: ObjetoCommands.ObjetoSnapshot
  ): Try[Response.SuccessProcessing] = {
    val event = ObjetoSnapshotPersisted(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.saldo,
      command.cotitulares,
      command.tags,
      command.sujetoResponsable,
      actor.state.porcentajeResponsabilidad,
      actor.state.registro,
      command.obligacionesSaldo
    )
    val consolidatedState = actor.state + event

    println(s"""
               |event ${event}
               | STATE ${actor.state}
               |COnsOLidATED STATE ${consolidatedState}
               |""".stripMargin)

    actor.persistSnapshot(event, consolidatedState) { () =>
      println(s"""
           |
           |  AFTER  actor.persistSnapshot(event, consolidatedState) { () =>
           |event ${event}
           | STATE ${actor.state}
           |COnsOLidATED STATE ${consolidatedState}
          |""".stripMargin)
      actor.state = consolidatedState
      actor.informParent(command, actor.state)
      actor.context.sender() ! Response.SuccessProcessing(command.deliveryId)
    }
    Success(Response.SuccessProcessing(command.deliveryId))
  }
}
