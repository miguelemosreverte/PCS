package design_principles.projection.mock

import cassandra.ReadSideProjection
import cassandra.ReadSideProjection
import scala.collection.mutable
import design_principles.projection.CassandraTestkit

class CassandraTestkitMock() extends CassandraTestkit {
  val rowsAsMap: mutable.Map[String, Map[String, String]] = mutable.Map.empty[String, Map[String, String]]
  val cassandraWrite: CassandraWriteMock = new CassandraWriteMock(rowsAsMap)
  val cassandraRead: CassandraReadMock = new CassandraReadMock(rowsAsMap)
}
