package cassandra.read

import java.util

import scala.concurrent.{ExecutionContext, Future}
import akka.actor.ActorSystem
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSession, CassandraSource}
import akka.stream.scaladsl.Sink
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.{Row, SimpleStatement}
import utils.implicits.RowT._
import scala.jdk.CollectionConverters._

class CassandraReadProduction(implicit session: CqlSession) extends CassandraRead {

  def cqlQuerySingleResult(cqlQuery: String): Option[Row] =
    session.execute(cqlQuery).all().asScala.headOption

  override def getRow(cql: String): Option[Map[String, String]] =
    cqlQuerySingleResult(cql).map { maybeRow =>
      maybeRow.toMap

    }
}
