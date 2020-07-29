package readside.proyectionists.registrales.subasta
import scala.concurrent.Future

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.projection.eventsourced.EventEnvelope
import akka.projections.ProjectionSettings
import akka.projections.cassandra.CassandraProjectionHandler
import akka.{Done, actor => classic}
import consumers.registral.subasta.domain.SubastaEvents
import org.slf4j.LoggerFactory
import readside.proyectionists.registrales.subasta.projections.SubastaUpdatedFromDtoProjection

class SubastaProjectionHandler(settings: ProjectionSettings, system: ActorSystem[_])
    extends CassandraProjectionHandler[SubastaEvents](settings, system) {
  implicit val classicSystem: classic.ActorSystem = system.toClassic
  import classicSystem.dispatcher
  private val log = LoggerFactory.getLogger(getClass)

  private val tag = settings.tag

  def this()(implicit classicSystem: akka.actor.ActorSystem) {
    this(ProjectionSettings("Subasta", 1), classicSystem.toTyped)
  }
  override def process(envelope: EventEnvelope[SubastaEvents]): Future[Done] = {
    envelope.event match {
      case evt: SubastaEvents.SubastaUpdatedFromDto =>
        log.debug(
          s"SubastaProjectionHandler consumed $evt from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        val projection = SubastaUpdatedFromDtoProjection(evt)
        cassandra writeState projection
      case other =>
        log.warn(
          s"SubastaProjectionHandler consumed $other from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        Future.successful(Done)
    }
  }
}
