package readside.proyectionists.no_registrales.obligacion
import scala.concurrent.{ExecutionContextExecutor, Future}

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.projection.eventsourced.EventEnvelope
import akka.projections.ProjectionSettings
import akka.projections.cassandra.CassandraProjectionHandler
import akka.stream.alpakka.cassandra.CassandraSessionSettings
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSession, CassandraSessionRegistry}
import akka.{Done, actor => classic}
import consumers.no_registral.obligacion.domain.ObligacionEvents
import org.slf4j.LoggerFactory
import readside.proyectionists.no_registrales.obligacion.projectionists.ObligacionSnapshotProjection

class ObligacionProjectionHandler(settings: ProjectionSettings, system: ActorSystem[_])
    extends CassandraProjectionHandler[ObligacionEvents](settings, system) {
  implicit val classicSystem: classic.ActorSystem = system.toClassic
  implicit val ec: ExecutionContextExecutor = classicSystem.dispatcher
  private val log = LoggerFactory.getLogger(getClass)
  private val tag = settings.tag

  val sessionSettings: CassandraSessionSettings = CassandraSessionSettings.create()
  implicit val session: CassandraSession = CassandraSessionRegistry.get(system).sessionFor(sessionSettings)

  override def stop(): Future[Done] = {
    session.close(ec)
    super.stop()
  }

  def this()(implicit classicSystem: akka.actor.ActorSystem) {
    this(ProjectionSettings("Obligacion", 1), classicSystem.toTyped)
  }

  // val message = EventProcessorPrinter.prettifyEventProcessorLog(eventEnvelope.toString)
  override def process(envelope: EventEnvelope[ObligacionEvents]): Future[Done] = {
    envelope.event match {
      case evt: ObligacionEvents.ObligacionPersistedSnapshot =>
        log.debug(
          s"ObligacionProjectionHandler consumed $evt from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        val projection = ObligacionSnapshotProjection(evt)
        cassandra writeState projection
      case evt: ObligacionEvents.ObligacionRemoved =>
        session
          .executeDDL(
            s"""
          DELETE FROM read_side.buc_obligaciones WHERE bob_suj_identificador = '${evt.sujetoId}' and bob_soj_tipo_objeto = '${evt.tipoObjeto}' and bob_soj_identificador = '${evt.objetoId}' and bob_obn_id = '${evt.obligacionId}'

          """
          )
          .recover { ex: Throwable =>
            log.error(ex.getMessage)
            ex
          }
        Future.successful(Done)

      case other =>
        log.warn(
          s"ObligacionProjectionHandler consumed $other from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        Future.successful(Done)
    }
  }
}
