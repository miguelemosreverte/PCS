package cassandra

import akka.Done
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.{BoundStatement, PreparedStatement}
import design_principles.actor_model.Event

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait ReadSideProjection[E <: Event] {

  class CassandraConnectionException extends Exception {
    override def getMessage: String = "Couldn't connect to Cassandra"
  }
  lazy val session: Either[CassandraConnectionException, CqlSession] = Try {
    CqlSessionSingleton.session
  }.toOption match {
    case Some(value) => Right(value)
    case None => Left(new CassandraConnectionException)
  }

  lazy val preparedStatement: Either[CassandraConnectionException, PreparedStatement] =
    session.map(_.prepare(statement))

  final protected def boundStatement: Either[CassandraConnectionException, BoundStatement] =
    preparedStatement.map(_ bind (binds: _*))

  def updateReadside()(implicit ec: ExecutionContext): Future[Done] = {
    import scala.jdk.FutureConverters._
    (for {
      s <- session
      b <- boundStatement
    } yield (s, b)) match {
      case Left(cassandraConnectionException) =>
        Future.failed(cassandraConnectionException)
      case Right((session, boundStatement)) =>
        session.executeAsync(boundStatement).asScala.map { _ =>
          akka.Done
        }
    }
  }

  def event: E

  def collectionName: String

  def bindings: List[(String, Any)]

  def keys: List[(String, Any)]

  final private def sets(b: List[(String, Object)]) =
    b.map(_._1)
      .map { k =>
        s"$k=?"
      }
      .mkString(",\n")

  final private def setsKeys(b: List[(String, Object)]) =
    b.map(_._1)
      .map { k =>
        s"$k=?"
      }
      .mkString(" AND ")

  final def statement: String = {
    s"""UPDATE $collectionName
         | SET ${sets(curatedBindings)}
         | WHERE ${setsKeys(curatedKeys)}
      """.stripMargin
  }

  final private def curatedBindings: List[(String, Object)] = CassandraTypesAdapter.curateKeyValueList(bindings)
  final private def curatedKeys: List[(String, Object)] = CassandraTypesAdapter.curateKeyValueList(keys)

  def binds: List[Object] = (curatedBindings ++ curatedKeys).map(_._2)

}
