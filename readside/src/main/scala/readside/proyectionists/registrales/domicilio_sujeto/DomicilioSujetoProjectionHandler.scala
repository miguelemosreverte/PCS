package readside.proyectionists.registrales.domicilio_sujeto
import scala.concurrent.Future
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.projection.eventsourced.EventEnvelope
import akka.projections.{ProjectionHandlerConfig, ProjectionSettings}
import akka.projections.cassandra.CassandraProjectionHandler
import akka.{Done, actor => classic}
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents
import monitoring.Monitoring
import org.slf4j.LoggerFactory
import readside.proyectionists.no_registrales.objeto.ObjetoProjectionHandler.defaultTag
import readside.proyectionists.registrales.domicilio_objeto.DomicilioObjetoProjectionHandler
import readside.proyectionists.registrales.domicilio_sujeto.projections.DomicilioSujetoUpdatedFromDtoProjection

class DomicilioSujetoProjectionHandler(settings: ProjectionSettings, system: ActorSystem[_])
    extends CassandraProjectionHandler[DomicilioSujetoEvents](settings, system) {
  implicit val classicSystem: classic.ActorSystem = system.toClassic
  import classicSystem.dispatcher
  private val tag = settings.tag

  override def processEnvelope(envelope: EventEnvelope[DomicilioSujetoEvents]): Future[Done] = {
    envelope.event match {
      case evt: DomicilioSujetoEvents.DomicilioSujetoUpdatedFromDto =>
        log.debug(
          s"DomicilioSujetoProjectionHandler consumed $evt from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        val projection = DomicilioSujetoUpdatedFromDtoProjection(evt)
        cassandra writeState projection
      case other =>
        log.warn(
          s"DomicilioSujetoProjectionHandler consumed $other from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        Future.successful(Done)
    }
  }
}

object DomicilioSujetoProjectionHandler {
  val defaultTag = "DomicilioSujeto"
  val defaultParallelism = ProjectionHandlerConfig.getThisTagParallelism(defaultTag)
  val defaultProjectionSettings: Monitoring => ProjectionSettings =
    ProjectionSettings.default(tag = defaultTag, parallelism = defaultParallelism)
  def apply(monitoring: Monitoring, system: ActorSystem[_]): DomicilioSujetoProjectionHandler = {
    val projectionSettings = defaultProjectionSettings(monitoring)
    new DomicilioSujetoProjectionHandler(projectionSettings, system)
  }
}
