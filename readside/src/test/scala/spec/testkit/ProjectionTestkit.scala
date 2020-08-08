package spec.testkit

import scala.concurrent.Future
import akka.Done
import akka.persistence.query.NoOffset
import akka.projection.eventsourced.EventEnvelope
import design_principles.projection.CassandraTestkit
import monitoring.{DummyMonitoring, Monitoring}
import utils.generators.Model.deliveryId

abstract class ProjectionTestkit[Events, AggregateRoot] {

  val monitoring: Monitoring = new DummyMonitoring
  val cassandraTestkit: CassandraTestkit

  def processEnvelope(envelope: EventEnvelope[Events]): Future[Done]

  def eventEnvelope(event: Events): EventEnvelope[Events] =
    EventEnvelope[Events](NoOffset, "", 1L, event, deliveryId)

  def read(id: AggregateRoot): Map[String, String]
}
