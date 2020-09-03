package cqrs.base_actor.untyped

import akka.Done
import akka.actor.{ActorContext, ActorSystem}
import akka.persistence.PersistentActor
import akka.stream.alpakka.cassandra.CassandraSessionSettings
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSession, CassandraSessionRegistry}
import com.datastax.oss.driver.api.core.cql.{BoundStatement, PreparedStatement}
import ddd.ReadSideProjection
import design_principles.actor_model.Event
import org.slf4j.LoggerFactory
import play.api.libs.json.Format

import scala.collection.mutable
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import akka.pattern.pipe

trait FastPersistentActor { a: PersistentActor =>
  val s: ActorSystem = a.context.system
  private val sessionSettings = CassandraSessionSettings.create()
  private implicit val session: CassandraSession = CassandraSessionRegistry.get(s).sessionFor(sessionSettings)
  private val logger = LoggerFactory.getLogger(this.getClass)

  final private val statement = {
    Await.result(
      session.prepare(s"""
        INSERT INTO eventsourcing.events 
        (aggregateRoot, event) 
        VALUES (?, ?);
      """.stripMargin),
      60 seconds
    )
  }

  var batch: mutable.Buffer[String] = mutable.Buffer.empty
  val persistenceId: String
  private final val aggregateRoot = persistenceId

  private implicit val ec = s.dispatcher

  def fastPersist[A](event: A)(handler: A => Unit): Unit = {
    val serializedEvent = event.toString // serialization.encode(event)
    val result = for {
      done <- session.executeWrite(statement.bind(aggregateRoot, serializedEvent))
    } yield done
    result.onComplete {
      case Failure(throwable) =>
        println(s"Cassandra failed with ${serializedEvent} due to ${throwable}")
        handler(event)
      case Success(value) =>
        println(s"Cassandra succeeded with value ${value}")
        handler(event)
    }
  }
}
