package spec.testkit

import design_principles.actor_model.Event
import design_principles.projection.mock.CassandraTestkitMock
import readside.proyectionists.common.shared.UpdateReadSideProjection

trait ProjectionTestkitMock[E <: Event, AggregateRoot] extends ProjectionTestkit[E, AggregateRoot] {

  type Snapshot
  type Projection <: UpdateReadSideProjection[E]
  val decode: String => Snapshot
  val project: Snapshot => Projection

  val cassandraTestkit: CassandraTestkitMock
  override def read(id: AggregateRoot): Map[String, String] = {
    println(s"Reading from cassandraTestkit.cassandraRead the following id: ${id.toString}")
    val value: Option[String] = cassandraTestkit.cassandraRead getEvent id.toString
    value
      .map { value =>
        val snapshot = decode(value)
        (
          project(snapshot).keys ++ project(snapshot).bindings
        ).map(t => (t._1, t._2.toString)).toMap
      }
      .getOrElse(Map.empty)
  }
}
