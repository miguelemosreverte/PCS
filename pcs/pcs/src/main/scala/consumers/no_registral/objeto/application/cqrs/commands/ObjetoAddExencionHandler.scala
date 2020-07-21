package consumers.no_registral.objeto.application.cqrs.commands

import akka.persistence.journal.Tagged
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.obligacion.application.entities.ObligacionCommands
import cqrs.untyped.command.CommandHandler.SyncCommandHandler

import scala.util.{Success, Try}

class ObjetoAddExencionHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.ObjetoAddExencion] {
  override def handle(
      command: ObjetoCommands.ObjetoAddExencion
  ): Try[akka.Done] = {
    val replyTo = actor.sender()
    val event = ObjetoEvents.ObjetoAddedExencion(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.exencion
    )

    val documentName = utils.Inference.getSimpleName(event.getClass.getName)
    val lastDeliveryId = actor.state.lastDeliveryIdByEvents.getOrElse(documentName, BigInt(0))
    if (event.deliveryId < lastDeliveryId) {
      replyTo ! akka.Done
    } else {
      actor.persistEvent(event, Set("Exencion")) { () =>
        log.info(s"[${actor.persistenceId}] Persist event | ${event}")
        actor.state += event
        log.info(
          s"[${actor.persistenceId}] will inform about exencion to obligacion ${actor.state.obligaciones.mkString(",")}"
        )
        actor.state.obligaciones.foreach { obligacionId =>
          val obligacion = actor.obligaciones((command.sujetoId, command.objetoId, command.tipoObjeto, obligacionId))
          log.info(s"[${actor.persistenceId}] Informing obligacion $obligacionId of the new exencion")
          obligacion ! ObligacionCommands.ObligacionUpdateExencion(command.deliveryId,
                                                                   command.sujetoId,
                                                                   command.objetoId,
                                                                   command.tipoObjeto,
                                                                   obligacionId.split("-").last,
                                                                   command.exencion)
        }
        actor.informParent(command, actor.state)
        replyTo ! akka.Done
      }
    }
    Success(akka.Done)
  }

}
