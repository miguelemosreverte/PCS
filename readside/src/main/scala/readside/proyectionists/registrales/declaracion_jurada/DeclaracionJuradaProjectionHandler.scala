package readside.proyectionists.registrales.declaracion_jurada
import scala.concurrent.Future
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.projection.eventsourced.EventEnvelope
import akka.projections.ProjectionSettings
import akka.projections.cassandra.CassandraProjectionHandler
import akka.{Done, actor => classic}
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents
import monitoring.Monitoring
import org.slf4j.LoggerFactory
import readside.proyectionists.registrales.declaracion_jurada.projections.DeclaracionJuradaUpdatedFromDtoProjection

class DeclaracionJuradaProjectionHandler(settings: ProjectionSettings, system: ActorSystem[_])
    extends CassandraProjectionHandler[DeclaracionJuradaEvents](settings, system) {
  implicit val classicSystem: classic.ActorSystem = system.toClassic
  import classicSystem.dispatcher
  private val tag = settings.tag

  override def process(envelope: EventEnvelope[DeclaracionJuradaEvents]): Future[Done] = {
    envelope.event match {
      case evt: DeclaracionJuradaEvents.DeclaracionJuradaUpdatedFromDto =>
        log.debug(
          s"DeclaracionJuradaProjectionHandler consumed $evt from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        val projection = DeclaracionJuradaUpdatedFromDtoProjection(evt)
        cassandra writeState projection
      case other =>
        log.warn(
          s"DeclaracionJuradaProjectionHandler consumed $other from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        Future.successful(Done)
    }
  }
}

object DeclaracionJuradaProjectionHandler {
  val defaultTag = "DeclaracionJurada"
  val defaultParallelism = 1
  val defaultProjectionSettings: Monitoring => ProjectionSettings =
    ProjectionSettings.default(tag = defaultTag, parallelism = defaultParallelism)
  def apply(monitoring: Monitoring, system: ActorSystem[_]): DeclaracionJuradaProjectionHandler = {
    val projectionSettings = defaultProjectionSettings(monitoring)
    new DeclaracionJuradaProjectionHandler(projectionSettings, system)

  }
}
