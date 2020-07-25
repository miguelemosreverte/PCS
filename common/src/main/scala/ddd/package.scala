import scala.concurrent.{ExecutionContext, Future}

import akka.stream.alpakka.cassandra.scaladsl.CassandraSession
import com.datastax.oss.driver.api.core.cql.BoundStatement
import design_principles.actor_model.Event
import org.slf4j.{Logger, LoggerFactory}

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

  trait ReadSideProjection[E <: Event] {
    def event: E
    def prepareStatement(session: CassandraSession)(implicit ec: ExecutionContext): Future[BoundStatement]
  }
}
