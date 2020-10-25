package design_principles.projection.mock

import scala.collection.mutable
import scala.concurrent.Future

import cassandra.read.CassandraRead
import org.scalatest.concurrent.ScalaFutures

class CassandraReadMock(rowsAsMap: mutable.Map[String, Map[String, String]]) extends CassandraRead with ScalaFutures {
  override def getRow(key: String): Option[Map[String, String]] =
    rowsAsMap.get(key)

}
