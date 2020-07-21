package readside.proyectionists.registrales.parametrica_plan
import scala.concurrent.Future

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.projection.eventsourced.EventEnvelope
import akka.projections.ProjectionSettings
import akka.projections.cassandra.CassandraProjectionHandler
import akka.{Done, actor => classic}
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents
import org.slf4j.LoggerFactory
import readside.proyectionists.registrales.parametrica_plan.projections.ParametricaPlanUpdatedFromDtoProjection

class ParametricaPlanProjectionHandler(settings: ProjectionSettings, system: ActorSystem[_])
    extends CassandraProjectionHandler[ParametricaPlanEvents](settings, system) {
  implicit val classicSystem: classic.ActorSystem = system.toClassic
  import classicSystem.dispatcher
  private val log = LoggerFactory.getLogger(getClass)

  private val tag = settings.tag

  def this()(implicit classicSystem: akka.actor.ActorSystem) {
    this(ProjectionSettings("ParametricaPlan", 1), classicSystem.toTyped)
  }

  override def process(envelope: EventEnvelope[ParametricaPlanEvents]): Future[Done] = {
    envelope.event match {
      case evt: ParametricaPlanEvents.ParametricaPlanUpdatedFromDto =>
        log.info(
          s"ParametricaPlanProjectionHandler consumed $evt from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        val projection = ParametricaPlanUpdatedFromDtoProjection(evt)
        cassandra writeState projection
      case other =>
        log.warn(
          s"ParametricaPlanProjectionHandler consumed $other from $tag with seqNr ${envelope.sequenceNr}",
          settings.tag,
          envelope.event,
          envelope.persistenceId,
          envelope.sequenceNr
        )
        Future.successful(Done)
    }
  }
}
