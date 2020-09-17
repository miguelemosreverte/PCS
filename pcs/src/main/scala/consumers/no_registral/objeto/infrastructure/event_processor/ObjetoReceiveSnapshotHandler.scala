package consumers.no_registral.objeto.infrastructure.event_processor

import akka.Done
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.cotitularidad.application.entities.CotitularidadCommands.{
  CotitularidadAddSujetoCotitular,
  CotitularidadPublishSnapshot
}
import consumers.no_registral.cotitularidad.infrastructure.json._
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoSnapshot
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoUpdatedFromTri
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import kafka.KafkaMessageProducer.KafkaKeyValue
import monitoring.Monitoring
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

class ObjetoReceiveSnapshotHandler(
    implicit
    requirements: MonitoringAndMessageProducer,
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObjetoSnapshot](requirements.monitoring) {
  implicit val ec: ExecutionContext = actorTransactionRequirements.executionContext
  val log = LoggerFactory.getLogger(this.getClass)
  def isResponsible(snapshot: ObjetoSnapshot): Boolean = snapshot.sujetoId == snapshot.sujetoResponsable
  def hasCotitulares(snapshot: ObjetoSnapshot): Boolean = snapshot.cotitulares.size > 1
  def shouldInformCotitulares(snapshot: ObjetoSnapshot): Boolean =
    isResponsible(snapshot) && hasCotitulares(snapshot)

  override def topic: String = "ObjetoReceiassveSnapshot"

  import consumers.no_registral.objeto.infrastructure.json._

  override def processInput(input: String): Either[Throwable, ObjetoSnapshot] =
    serialization.maybeDecode[ObjetoSnapshot](input)

  override def processMessage(evt: ObjetoSnapshot): Future[Response.SuccessProcessing] = {
    if (shouldInformCotitulares(evt)) {
      val publishedMessages: Set[Future[Done]] = evt.cotitulares.filter(_ == evt.sujetoResponsable) map { cotitular =>
        val message =
          CotitularidadPublishSnapshot(
            evt.deliveryId,
            sujetoId = cotitular,
            evt.objetoId,
            evt.tipoObjeto,
            evt.saldo,
            //evt.cotitulares,
            evt.tags,
            evt.obligacionesSaldo
          )
        requirements.messageProducer.produce(
          data = Seq(
            KafkaKeyValue(
              aggregateRoot = message.aggregateRoot,
              json = serialization.encode[CotitularidadPublishSnapshot](message)
            )
          ),
          topic = "CotitularidadPublishSnapshot"
        )(_ => log.debug(s"[ObjetoSnapshotHandler] Published message | CotitularidadPublishSnapshot"))

      }
      for {
        _ <- Future sequence publishedMessages
      } yield SuccessProcessing(evt.deliveryId)
    } else {
      Future.successful(SuccessProcessing(evt.deliveryId))
    }

  }

}
