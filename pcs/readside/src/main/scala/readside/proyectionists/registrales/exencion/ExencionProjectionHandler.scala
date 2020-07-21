package readside.proyectionists.registrales.exencion
import scala.concurrent.Future

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.projection.eventsourced.EventEnvelope
import akka.projections.ProjectionSettings
import akka.projections.cassandra.CassandraProjectionHandler
import akka.{Done, actor => classic}
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoAddedExencion
import org.slf4j.LoggerFactory
import readside.proyectionists.registrales.exencion.projections.ObjetoAddedExencionProjection

class ExencionProjectionHandler(settings: ProjectionSettings, system: ActorSystem[_])
    extends CassandraProjectionHandler[ObjetoAddedExencion](settings, system) {
  implicit val classicSystem: classic.ActorSystem = system.toClassic
  import classicSystem.dispatcher
  private val log = LoggerFactory.getLogger(getClass)

  private val tag = settings.tag

  def this()(implicit classicSystem: akka.actor.ActorSystem) {
    this(ProjectionSettings("Exencion", 1), classicSystem.toTyped)
  }

  override def process(envelope: EventEnvelope[ObjetoAddedExencion]): Future[Done] = {
    envelope.event match {
      case evt: ObjetoEvents.ObjetoAddedExencion =>
        log.info(
          s"ExencionProjectionHandler consumed $evt from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        val projection = ObjetoAddedExencionProjection(evt)
        cassandra writeState projection
      case other =>
        log.warn(
          s"ExencionProjectionHandler consumed $other from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        Future.successful(Done)
    }
  }
}
