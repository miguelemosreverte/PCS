package readside.proyectionists.no_registrales.objeto
import scala.concurrent.Future
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.projection.eventsourced.EventEnvelope
import akka.projections.ProjectionSettings
import akka.projections.cassandra.CassandraProjectionHandler
import akka.{Done, actor => classic}
import consumers.no_registral.objeto.domain.ObjetoEvents
import monitoring.Monitoring
import org.slf4j.LoggerFactory
import readside.proyectionists.no_registrales.objeto.projections.ObjetoSnapshotPersistedProjection

class ObjetoProjectionHandler(settings: ProjectionSettings, system: ActorSystem[_])
    extends CassandraProjectionHandler[ObjetoEvents](settings, system) {
  implicit val classicSystem: classic.ActorSystem = system.toClassic
  import classicSystem.dispatcher
  private val log = LoggerFactory.getLogger(getClass)

  private val tag = settings.tag

  def this(monitoring: Monitoring)(implicit classicSystem: akka.actor.ActorSystem) {
    this(ProjectionSettings("Objeto", 1, monitoring), classicSystem.toTyped)
  }

  override def process(envelope: EventEnvelope[ObjetoEvents]): Future[Done] = {
    envelope.event match {
      case evt: ObjetoEvents.ObjetoSnapshotPersisted =>
        log.debug(
          s"ObjetoProjectionHandler consumed $evt from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        val projection = ObjetoSnapshotPersistedProjection(evt)
        cassandra writeState projection
      case other =>
        log.warn(
          s"ObjetoProjectionHandler consumed $other from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        Future.successful(Done)
    }
  }
}
