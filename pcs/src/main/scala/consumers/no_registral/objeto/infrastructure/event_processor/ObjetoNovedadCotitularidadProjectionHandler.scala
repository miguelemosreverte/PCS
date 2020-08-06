package consumers.no_registral.objeto.infrastructure.event_processor

import scala.concurrent.{ExecutionContextExecutor, Future}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.server.Route
import akka.kafka.ProducerSettings
import akka.projection.eventsourced.EventEnvelope
import akka.projections.ProjectionSettings
import akka.projections.cassandra.CassandraProjectionHandler
import akka.{Done, actor => classic}
import consumers.no_registral.cotitularidad.application.entities.CotitularidadCommands.{
  CotitularidadAddSujetoCotitular,
  CotitularidadPublishSnapshot
}
import consumers.no_registral.cotitularidad.infrastructure.json._
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import kafka.KafkaMessageProcessorRequirements
import kafka.KafkaProducer.produce
import monitoring.Monitoring
import org.slf4j.LoggerFactory

class ObjetoNovedadCotitularidadProjectionHandler(settings: ProjectionSettings, system: ActorSystem[_])
    extends CassandraProjectionHandler[ObjetoEvents](settings, system) {
  implicit val classicSystem: classic.ActorSystem = system.toClassic
  implicit val ec: ExecutionContextExecutor = classicSystem.dispatcher
  private val log = LoggerFactory.getLogger(getClass)

  override def process(envelope: EventEnvelope[ObjetoEvents]): Future[Done] =
    processEvent(envelope.event)

  def processEvent(event: ObjetoEvents): Future[Done] = {
    event match {
      case evt: ObjetoEvents.ObjetoUpdatedFromTri =>
        val topic = "AddCotitularTransaction"
        val message =
          CotitularidadAddSujetoCotitular(
            deliveryId = evt.deliveryId,
            sujetoId = evt.sujetoId,
            objetoId = evt.objetoId,
            tipoObjeto = evt.tipoObjeto,
            isResponsable = evt.isResponsable,
            sujetoResponsable = evt.sujetoResponsable
          )

        val messages = Seq(message) map serialization.encode[CotitularidadAddSujetoCotitular]
        publishMessageToKafka(messages, topic)

      case evt: ObjetoEvents.ObjetoSnapshotPersisted if shouldInformCotitulares(evt) =>
        val publishedMessages: Set[Future[Done]] = evt.cotitulares.filter(_ == evt.sujetoResponsable) map { cotitular =>
          val topic = "CotitularidadPublishSnapshot"
          val message =
            CotitularidadPublishSnapshot(
              evt.deliveryId,
              sujetoId = cotitular,
              evt.objetoId,
              evt.tipoObjeto,
              evt.saldo,
              //evt.cotitulares,
              evt.vencimiento,
              evt.tags,
              evt.obligacionesSaldo,
              evt.obligacionesVencidasSaldo
            )

          val messages = Seq(message) map serialization.encode[CotitularidadPublishSnapshot]
          publishMessageToKafka(messages, topic)

        }
        for {
          _ <- Future sequence publishedMessages
        } yield Done

      case _ => Future.successful(Done)
    }
  }

  def publishMessageToKafka(messages: Seq[String], topic: String): Future[Done] = {
    implicit val producerSettings: ProducerSettings[String, String] =
      KafkaMessageProcessorRequirements.productionSettings(None, settings.monitoring, system.toClassic).producer
    produce(messages, topic)(_ =>
      log.debug(s"[ObjetoNovedadCotitularidad] Published message | CotitularidadAddSujetoCotitular")
    )
  }

  def isResponsible(snapshot: ObjetoSnapshotPersisted): Boolean = snapshot.sujetoId == snapshot.sujetoResponsable
  def hasCotitulares(snapshot: ObjetoSnapshotPersisted): Boolean = snapshot.cotitulares.size > 1
  def shouldInformCotitulares(snapshot: ObjetoSnapshotPersisted): Boolean =
    isResponsible(snapshot) && hasCotitulares(snapshot)
}

object ObjetoNovedadCotitularidadProjectionHandler {
  def apply(monitoring: Monitoring, system: ActorSystem[_]): ObjetoNovedadCotitularidadProjectionHandler = {
    val objetoSettings = ProjectionSettings("ObjetoNovedadCotitularidad", 1, monitoring)
    new ObjetoNovedadCotitularidadProjectionHandler(objetoSettings, system)
  }
}
