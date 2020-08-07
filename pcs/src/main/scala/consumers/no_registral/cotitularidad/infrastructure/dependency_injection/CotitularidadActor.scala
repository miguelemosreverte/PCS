package consumers.no_registral.cotitularidad.infrastructure.dependency_injection

import akka.actor.Props
import akka.actor.Status.Success
import akka.entity.ShardedEntity
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import consumers.no_registral.cotitularidad.application.entities.{
  CotitularidadCommands,
  CotitularidadQueries,
  CotitularidadResponses
}
import consumers.no_registral.cotitularidad.domain.{CotitularidadEvents, CotitularidadState}
import consumers.no_registral.objeto.application.entities.ObjetoCommands.{ObjetoSnapshot, ObjetoUpdateCotitulares}
import consumers.no_registral.objeto.infrastructure.json._
import design_principles.actor_model.Response
import kafka.{KafkaMessageProcessorRequirements, KafkaMessageProducer}
import utils.implicits.StringT._

class CotitularidadActor(transactionRequirements: KafkaMessageProcessorRequirements)
    extends PersistentActor
    with akka.actor.ActorLogging {
  implicit val producerSettings = transactionRequirements.producer

  import context.system

  var state = CotitularidadState()

  var snapshot: ObjetoSnapshot =
    ObjetoSnapshot(
      deliveryId = 0,
      sujetoId = "",
      objetoId = "",
      tipoObjeto = "",
      saldo = 0,
      cotitulares = Set.empty,
      tags = Set.empty,
      vencimiento = false,
      sujetoResponsable = "",
      obligacionesSaldo = Map.empty,
      obligacionesVencidasSaldo = Map.empty
    )

  def obligacionId = self.path.name

  val kafka = new KafkaMessageProducer()
  def publishToKafka(message: Seq[String], topic: String) =
    kafka.produce(message, topic) _

  override def receiveCommand: Receive = {

    case query: CotitularidadQueries.GetCotitulares =>
      sender() ! CotitularidadResponses.GetCotitularesResponse(
        query.objetoId,
        query.tipoObjeto,
        state.sujetosCotitulares,
        state.sujetoResponsable,
        state.fechaUltMod
      )
    case command: CotitularidadCommands.CotitularidadAddSujetoCotitular =>
      val replyTo = sender()
      val event = CotitularidadEvents.CotitularidadAddedSujetoCotitular(
        command.deliveryId,
        command.sujetoId,
        command.objetoId,
        command.tipoObjeto,
        command.isResponsable,
        command.sujetoResponsable
      )
      persist(event) { _ =>
        log.debug(s"[$persistenceId] Persist event | $event")

        state += event

        log.debug(s"[$persistenceId] Cotitulares: | ${state}")

        snapshot = snapshot.copy(
          sujetoId = command.sujetoId,
          objetoId = command.objetoId,
          tipoObjeto = command.tipoObjeto,
          sujetoResponsable = state.sujetoResponsable,
          cotitulares = state.sujetosCotitulares
        )

        val informCotitularesOfAddedCotitular =
          if (state.sujetosCotitulares.size == 1)
            Seq.empty[ObjetoUpdateCotitulares]
          else
            state.sujetosCotitulares.map { cotitular =>
              snapshot.copy(sujetoId = cotitular, cotitulares = state.sujetosCotitulares)
              ObjetoUpdateCotitulares(
                deliveryId = command.deliveryId,
                sujetoId = cotitular,
                objetoId = command.objetoId,
                tipoObjeto = command.tipoObjeto,
                cotitulares = state.sujetosCotitulares
              )
            }.toSeq

        val topic = "ObjetoUpdateCotitularesTransaction"
        val messages = informCotitularesOfAddedCotitular map serialization.encode[ObjetoUpdateCotitulares]
        if (messages.nonEmpty)
          publishToKafka(messages, topic) { _ =>
            log.debug(
              s"[$persistenceId] Published message | ObjetoSnapshot to Sujeto(${command.sujetoId})"
            )
            context.sender() ! Response.SuccessProcessing(command.deliveryId)
          }

        context.sender() ! Response.SuccessProcessing(command.deliveryId)
      }

    case command: CotitularidadCommands.CotitularidadPublishSnapshot =>
      val replyTo = sender()
      val topic = "ObjetoReceiveSnapshot"
      val messages = state.sujetosCotitulares.filter(_ != state.sujetoResponsable).map { cotitular =>
          snapshot = ObjetoSnapshot(
            deliveryId = command.deliveryId,
            sujetoId = cotitular,
            objetoId = command.objetoId,
            tipoObjeto = command.tipoObjeto,
            saldo = command.saldo,
            cotitulares = state.sujetosCotitulares,
            tags = command.tags,
            vencimiento = command.vencimiento,
            sujetoResponsable = state.sujetoResponsable,
            obligacionesSaldo = command.obligacionesSaldo,
            obligacionesVencidasSaldo = command.obligacionesVencidasSaldo
          )

          snapshot
        } map serialization.encode[ObjetoSnapshot]

      if (messages.nonEmpty)
        publishToKafka(messages.toSeq, topic) { _ =>
          log.debug(
            s"[$persistenceId] Published message | ObjetoSnapshot to Sujeto(${state.sujetosCotitulares.mkString(",")})"
          )
          context.sender() ! Response.SuccessProcessing(command.deliveryId)
        }

    case other => log.error(s"[$persistenceId] Unexpected message |  ${other}")

  }

  val persistenceId = self.path.name
  override def receiveRecover: Receive = {
    case evt: CotitularidadEvents =>
      state += evt
    case SnapshotOffer(_, snapshot: CotitularidadState) =>
      state = snapshot
    case r: RecoveryCompleted =>
    case unknown =>
      log.error(s"[$persistenceId] error received recover with [$unknown]")
  }

}

object CotitularidadActor extends ShardedEntity[KafkaMessageProcessorRequirements] {
  def props(requirements: KafkaMessageProcessorRequirements): Props = Props(new CotitularidadActor(requirements))
}
