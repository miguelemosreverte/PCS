package readside.proyectionists.registrales.domicilio_objeto
import scala.concurrent.Future

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.projection.eventsourced.EventEnvelope
import akka.projections.ProjectionSettings
import akka.projections.cassandra.CassandraProjectionHandler
import akka.{Done, actor => classic}
import consumers.registral.domicilio_objeto.domain.DomicilioObjetoEvents
import org.slf4j.LoggerFactory
import readside.proyectionists.registrales.domicilio_objeto.projections.DomicilioObjetoUpdatedFromDtoProjection

class DomicilioObjetoProjectionHandler(settings: ProjectionSettings, system: ActorSystem[_])
    extends CassandraProjectionHandler[DomicilioObjetoEvents](settings, system) {
  implicit val classicSystem: classic.ActorSystem = system.toClassic
  import classicSystem.dispatcher
  private val log = LoggerFactory.getLogger(getClass)

  private val tag = settings.tag

  def this()(implicit classicSystem: akka.actor.ActorSystem) {
    this(ProjectionSettings("DomicilioObjeto", 1), classicSystem.toTyped)
  }

  override def process(envelope: EventEnvelope[DomicilioObjetoEvents]): Future[Done] = {
    envelope.event match {
      case evt: DomicilioObjetoEvents.DomicilioObjetoUpdatedFromDto =>
        log.info(
          s"DomicilioObjetoProjectionHandler consumed $evt from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        val projection = DomicilioObjetoUpdatedFromDtoProjection(evt)
        cassandra writeState projection
      case other =>
        log.warn(
          s"DomicilioObjetoProjectionHandler consumed $other from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        Future.successful(Done)
    }
  }
}
