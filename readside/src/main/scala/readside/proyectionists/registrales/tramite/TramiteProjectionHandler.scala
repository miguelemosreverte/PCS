package readside.proyectionists.registrales.tramite
import scala.concurrent.Future
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.projection.eventsourced.EventEnvelope
import akka.projections.ProjectionSettings
import akka.projections.cassandra.CassandraProjectionHandler
import akka.{Done, actor => classic}
import consumers.registral.tramite.domain.TramiteEvents
import monitoring.Monitoring
import org.slf4j.LoggerFactory
import readside.proyectionists.registrales.tramite.projections.TramiteUpdatedFromDtoProjection

class TramiteProjectionHandler(settings: ProjectionSettings, system: ActorSystem[_])
    extends CassandraProjectionHandler[TramiteEvents](settings, system) {
  implicit val classicSystem: classic.ActorSystem = system.toClassic
  import classicSystem.dispatcher
  private val tag = settings.tag

  override def process(envelope: EventEnvelope[TramiteEvents]): Future[Done] = {
    envelope.event match {
      case evt: TramiteEvents.TramiteUpdatedFromDto =>
        log.debug(
          s"TramiteProjectionHandler consumed $evt from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        val projection = TramiteUpdatedFromDtoProjection(evt)
        cassandra writeState projection
      case other =>
        log.warn(
          s"TramiteProjectionHandler consumed $other from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        Future.successful(Done)
    }
  }
}

object TramiteProjectionHandler {
  val defaultTag = "Tramite"
  val defaultParallelism = 1
  val defaultProjectionSettings: Monitoring => ProjectionSettings =
    ProjectionSettings.default(tag = defaultTag, parallelism = defaultParallelism)
  def apply(monitoring: Monitoring, system: ActorSystem[_]): TramiteProjectionHandler = {
    val projectionSettings = defaultProjectionSettings(monitoring)
    new TramiteProjectionHandler(projectionSettings, system)
  }
}
