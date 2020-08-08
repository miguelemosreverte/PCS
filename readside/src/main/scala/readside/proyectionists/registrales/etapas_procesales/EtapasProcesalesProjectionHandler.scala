package readside.proyectionists.registrales.etapas_procesales
import scala.concurrent.Future
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.projection.eventsourced.EventEnvelope
import akka.projections.ProjectionSettings
import akka.projections.cassandra.CassandraProjectionHandler
import akka.{Done, actor => classic}
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents
import monitoring.Monitoring
import org.slf4j.LoggerFactory
import readside.proyectionists.registrales.etapas_procesales.projections.EtapasProcesalesUpdatedFromDtoProjection

class EtapasProcesalesProjectionHandler(settings: ProjectionSettings, system: ActorSystem[_])
    extends CassandraProjectionHandler[EtapasProcesalesEvents](settings, system) {
  implicit val classicSystem: classic.ActorSystem = system.toClassic
  import classicSystem.dispatcher
  private val tag = settings.tag

  override def processEnvelope(envelope: EventEnvelope[EtapasProcesalesEvents]): Future[Done] = {
    envelope.event match {
      case evt: EtapasProcesalesEvents.EtapasProcesalesUpdatedFromDto =>
        log.debug(
          s"EtapasProcesalesProjectionHandler consumed $evt from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        val projection = EtapasProcesalesUpdatedFromDtoProjection(evt)
        cassandra writeState projection
      case other =>
        log.warn(
          s"EtapasProcesalesProjectionHandler consumed $other from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        Future.successful(Done)
    }
  }
}

object EtapasProcesalesProjectionHandler {
  val defaultTag = "EtapasProcesales"
  val defaultParallelism = 3
  val defaultProjectionSettings: Monitoring => ProjectionSettings =
    ProjectionSettings.default(tag = defaultTag, parallelism = defaultParallelism)
  def apply(monitoring: Monitoring, system: ActorSystem[_]): EtapasProcesalesProjectionHandler = {
    val projectionSettings = defaultProjectionSettings(monitoring)
    new EtapasProcesalesProjectionHandler(projectionSettings, system)
  }
}
