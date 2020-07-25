package spec.testkit

import scala.concurrent.Future

import akka.Done
import akka.persistence.query.NoOffset
import akka.projection.eventsourced.EventEnvelope
import design_principles.projection.CassandraTestkit
import utils.generators.Model.deliveryId

abstract class ProjectionTestkit[Events, AggregateRoot] {

  val cassandraTestkit: CassandraTestkit

  def process(envelope: EventEnvelope[Events]): Future[Done]

  def eventEnvelope(event: Events): EventEnvelope[Events] =
    EventEnvelope[Events](NoOffset, "", 1L, event, deliveryId)

  def read(id: AggregateRoot): Map[String, String]
}
