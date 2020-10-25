import akka.Done

import scala.concurrent.{ExecutionContext, Future}
import akka.stream.alpakka.cassandra.scaladsl.CassandraSession
import cassandra.{CassandraTypesAdapter, CqlSessionSingleton}
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.{BoundStatement, PreparedStatement}
import design_principles.actor_model.Event
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Try

package object ddd {

  trait Deliverable {
    def deliveryId: BigInt
  }

  trait AggregateRootExtractor[Ids] {
    def extractIds(givenAggregateRoot: String): Ids
  }

  trait AbstractState[Event] {
    def +(e: Event): AbstractState[Event]
    val log: Logger = LoggerFactory.getLogger(this.getClass)
  }

  trait ExternalDto
}
