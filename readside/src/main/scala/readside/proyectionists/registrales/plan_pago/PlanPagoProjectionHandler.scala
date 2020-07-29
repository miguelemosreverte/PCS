package readside.proyectionists.registrales.plan_pago
import scala.concurrent.Future

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.projection.eventsourced.EventEnvelope
import akka.projections.ProjectionSettings
import akka.projections.cassandra.CassandraProjectionHandler
import akka.{Done, actor => classic}
import consumers.registral.plan_pago.domain.PlanPagoEvents
import org.slf4j.LoggerFactory
import readside.proyectionists.registrales.plan_pago.projections.PlanPagoUpdatedFromDtoProjection

class PlanPagoProjectionHandler(settings: ProjectionSettings, system: ActorSystem[_])
    extends CassandraProjectionHandler[PlanPagoEvents](settings, system) {
  implicit val classicSystem: classic.ActorSystem = system.toClassic
  import classicSystem.dispatcher
  private val log = LoggerFactory.getLogger(getClass)

  private val tag = settings.tag

  def this()(implicit classicSystem: akka.actor.ActorSystem) {
    this(ProjectionSettings("ParametricaRecargo", 1), classicSystem.toTyped)
  }

  override def process(envelope: EventEnvelope[PlanPagoEvents]): Future[Done] = {
    envelope.event match {
      case evt: PlanPagoEvents.PlanPagoUpdatedFromDto =>
        log.debug(
          s"PlanPagoProjectionHandler consumed $evt from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        val projection = PlanPagoUpdatedFromDtoProjection(evt)
        cassandra writeState projection
      case other =>
        log.warn(
          s"PlanPagoProjectionHandler consumed $other from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        Future.successful(Done)
    }
  }
}