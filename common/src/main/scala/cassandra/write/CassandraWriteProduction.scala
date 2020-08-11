package cassandra.write

import java.time.Duration

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.cassandra.scaladsl.CassandraSession
import ddd.ReadSideProjection
import design_principles.actor_model.Event
import org.slf4j.LoggerFactory

class CassandraWriteProduction(implicit session: CassandraSession) extends CassandraWrite {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def writeState[E <: Event](
      state: ReadSideProjection[E]
  )(implicit system: ActorSystem, ec: ExecutionContext): Future[Done] = {
    val result = for {
      boundStmt <- state.prepareStatement(session)
      done <- session.executeWrite(boundStmt.setTimeout(Duration.ofSeconds(5)))
    } yield done
    result.onComplete {
      case Failure(throwable) =>
        logger.warn("Cassandra failed with {} due to {}", state.event.toString, throwable.toString)
      case Success(value) =>
        logger.debug("Cassandra succeeded with value {}", value.toString)
    }
    result
  }

  override def cql(cql: String): Future[Done] =
    session.executeDDL(cql)
}
