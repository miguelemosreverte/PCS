package consumers.no_registral.objeto.infrastructure.event_processor

import akka.entity.ShardedEntity.MonitoringAndMessageProducer

import scala.concurrent.{ExecutionContext, Future}
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.cotitularidad.application.entities.CotitularidadCommands.{
  CotitularidadAddSujetoCotitular,
  CotitularidadPublishSnapshot
}
import consumers.no_registral.cotitularidad.infrastructure.json._
import consumers.no_registral.objeto.domain.ObjetoEvents.{ObjetoSnapshotPersisted, ObjetoUpdatedFromTri}
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import kafka.KafkaMessageProducer.KafkaKeyValue
import monitoring.Monitoring
import org.slf4j.LoggerFactory

class ObjetoUpdatedFromTriHandler()(
    implicit
    r: MonitoringAndMessageProducer,
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[ObjetoUpdatedFromTri](r.monitoring) {
  implicit val ec: ExecutionContext = actorTransactionRequirements.executionContext
  val log = LoggerFactory.getLogger(this.getClass)

  override def topic: String = "ObjetoUpdatedFromTri"

  import consumers.no_registral.objeto.infrastructure.json._

  override def processInput(input: String): Either[Throwable, ObjetoUpdatedFromTri] =
    serialization.maybeDecode[ObjetoUpdatedFromTri](input)

  override def processMessage(evt: ObjetoUpdatedFromTri): Future[Response.SuccessProcessing] = {
    val message =
      CotitularidadAddSujetoCotitular(
        deliveryId = evt.deliveryId,
        sujetoId = evt.sujetoId,
        objetoId = evt.objetoId,
        tipoObjeto = evt.tipoObjeto,
        isResponsable = evt.isResponsable,
        sujetoResponsable = evt.sujetoResponsable
      )
    r.messageProducer
      .produce(
        data = Seq(
          KafkaKeyValue(
            aggregateRoot = message.aggregateRoot,
            json = serialization.encode[CotitularidadAddSujetoCotitular](message)
          )
        ),
        topic = "AddSujetoCotitularTransaction"
      ) { _ =>
        log.debug(s"[ObjetoUpdatedFromTriHandler] Published message | AddSujetoCotitularTransaction")
      }
      .map(_ => SuccessProcessing(evt.deliveryId))

  }

}
