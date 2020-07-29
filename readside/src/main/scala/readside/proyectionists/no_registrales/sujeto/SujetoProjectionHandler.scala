package readside.proyectionists.no_registrales.sujeto
import scala.concurrent.Future

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.projection.eventsourced.EventEnvelope
import akka.projections.ProjectionSettings
import akka.projections.cassandra.CassandraProjectionHandler
import akka.{Done, actor => classic}
import consumers.no_registral.sujeto.domain.SujetoEvents
import org.slf4j.LoggerFactory
import readside.proyectionists.no_registrales.sujeto.projections.SujetoSnapshotPersistedProjection

class SujetoProjectionHandler(settings: ProjectionSettings, system: ActorSystem[_])
    extends CassandraProjectionHandler[SujetoEvents](settings, system) {
  implicit val classicSystem: classic.ActorSystem = system.toClassic
  import classicSystem.dispatcher
  private val log = LoggerFactory.getLogger(getClass)

  private val tag = settings.tag

  def this()(implicit classicSystem: akka.actor.ActorSystem) {
    this(ProjectionSettings("Sujeto", 1), classicSystem.toTyped)
  }

  // val message = EventProcessorPrinter.prettifyEventProcessorLog(eventEnvelope.toString)
  override def process(envelope: EventEnvelope[SujetoEvents]): Future[Done] = {
    envelope.event match {
      case evt: SujetoEvents.SujetoSnapshotPersisted =>
        log.debug(
          s"SujetoProjectionHandler consumed $evt from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        val projection = SujetoSnapshotPersistedProjection(evt)
        cassandra writeState projection
      case other =>
        log.warn(
          s"SujetoProjectionHandler consumed $other from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        Future.successful(Done)
    }
  }
}