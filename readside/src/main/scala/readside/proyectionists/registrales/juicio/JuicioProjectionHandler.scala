package readside.proyectionists.registrales.juicio
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.projection.eventsourced.EventEnvelope
import akka.projections.ProjectionSettings
import akka.{Done, actor => classic}
import consumers.registral.juicio.domain.JuicioEvents
import org.slf4j.LoggerFactory
import readside.proyectionists.registrales.juicio.projections.JuicioUpdatedFromDtoProjection

import scala.concurrent.Future
import akka.projections.cassandra.CassandraProjectionHandler
import monitoring.Monitoring

class JuicioProjectionHandler(settings: ProjectionSettings, system: ActorSystem[_])
    extends CassandraProjectionHandler[JuicioEvents](settings, system) {
  implicit val classicSystem: classic.ActorSystem = system.toClassic
  import classicSystem.dispatcher
  private val tag = settings.tag

  def this(monitoring: Monitoring)(implicit classicSystem: akka.actor.ActorSystem) {
    this(ProjectionSettings("Juicio", 1, monitoring), classicSystem.toTyped)
  }
  override def processEnvelope(envelope: EventEnvelope[JuicioEvents]): Future[Done] = {
    envelope.event match {
      case evt: JuicioEvents.JuicioUpdatedFromDto =>
        log.debug(
          s"JuicioProjectionHandler consumed $evt from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        val projection = JuicioUpdatedFromDtoProjection(evt)
        cassandra writeState projection
      case other =>
        log.warn(
          s"JuicioProjectionHandler consumed $other from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        Future.successful(Done)
    }
  }
}

object JuicioProjectionHandler {
  val defaultTag = "Juicio"
  val defaultParallelism = 3
  val defaultProjectionSettings: Monitoring => ProjectionSettings =
    ProjectionSettings.default(tag = defaultTag, parallelism = defaultParallelism)
  def apply(monitoring: Monitoring, system: ActorSystem[_]): JuicioProjectionHandler = {
    val projectionSettings = defaultProjectionSettings(monitoring)
    new JuicioProjectionHandler(projectionSettings, system)
  }
}
