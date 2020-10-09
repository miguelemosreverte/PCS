package design_principles.projection.mock

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import akka.{actor, Done}
import cassandra.ReadSideProjection
import cassandra.write.CassandraWrite
import cassandra.ReadSideProjection
import design_principles.actor_model.Event

class CassandraWriteMock(rowsAsMap: mutable.Map[String, Map[String, String]]) extends CassandraWrite {

  val proyectionistReaction: ReadSideProjection[_ <: Event] => (String, Map[String, String]) = {
    case e: ReadSideProjection[_] =>
      (e.event.aggregateRoot,
       (
         e.keys ++ e.bindings
       ).map(t => (t._1, t._2.toString)).toMap)

  }

  override def writeState[E <: Event](
      state: ReadSideProjection[E]
  )(implicit ec: ExecutionContext): Future[Done] = {
    rowsAsMap.addOne(proyectionistReaction(state))
    Future.successful(Done)
  }

  override def cql(cql: String)(implicit ec: ExecutionContext): Future[Done] = {
    cql match {
      case s"Delete this row: $key" =>
        rowsAsMap.remove(key)
    }
    Future.successful(Done)
  }
}
