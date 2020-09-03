package cqrs.base_actor.untyped

import akka.Done

import scala.reflect.ClassTag
import scala.util.Try
import akka.persistence.journal.Tagged
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.PreparedStatement
import com.typesafe.config.{Config, ConfigFactory}
import cqrs.base_actor.untyped.SaveToCassandraActor.{session, statement}
import cqrs.untyped.event.{EventBus, SyncEventBus}
import ddd.AbstractState
import design_principles.actor_model.mechanism.local_processing.LocalizedProcessingMessageExtractor
import design_principles.actor_model.{Command, Event, Query}
import monitoring.{Counter, Monitoring}
import scala.compat.java8.FutureConverters._

object PersistentBaseActor {
  private implicit val session: CqlSession = CqlSession.builder().build()
  final private val statement: PreparedStatement = session.prepare(s"""
              INSERT INTO eventsourcing.events 
              (aggregateRoot, event) 
              VALUES (?, ?);
            """.stripMargin)
}
abstract class PersistentBaseActor[E <: Event: ClassTag, State <: AbstractState[E]: ClassTag](monitoring: Monitoring,
                                                                                              config: Config)
    extends BaseActor[E, State](monitoring)
    with PersistentActor {

  val persistedCounter: Counter = monitoring.counter(s"$name-persisted")

  val eventBus: EventBus[Try] = new SyncEventBus(logger)

  override def receive: Receive = super[PersistentActor].receive
  override def receiveCommand: Receive = {
    case cmd: Command =>
      commandBus.publish(cmd)
    case query: Query =>
      queryBus.ask(query)
    case other =>
      logger.warn(s"[$persistenceId]Unexpected message $other")
  }

  override def receiveRecover: Receive = {
    case e: E =>
      (state + e) match {
        case s: State => state = s
        case e =>
          throw new Exception(
            "Unexpectedly an AbstractState[Event] returned a different AbstractState of the same Event"
          )
      }

    case SnapshotOffer(_, snapshot: State) =>
      state = snapshot

    case RecoveryCompleted =>
      logger.debug(s"[$persistenceId] RecoveryCompleted")

    case other =>
      logger.warn(s"[$persistenceId] Unexpected event $other")
  }

  import PersistentBaseActor._
  import akka.pattern.pipe
  implicit val ec = context.system.dispatcher
  val aggregateRoot = persistenceId
  def persistEvent(event: E, tags: Set[String] = Set.empty)(handler: () => Unit = () => ()): Unit = {
    val tagsWithShardId = tags map { tag =>
      val parallelism = Try {
        config
          .getString(
            s"projectionist.$tag.paralellism"
          )
          .toInt
      }.getOrElse(1)
      val shardId = persistenceId.hashCode.abs % parallelism
      s"$tag-$shardId"
    }
    //persist(if (tags.nonEmpty) Tagged(event, tagsWithShardId) else event) { _ =>

    session
      .executeAsync(statement.bind(aggregateRoot, event.toString))
      .toScala
      .map { _ =>
        logger.debug(s"[$persistenceId] Persist event | $event")
        persistedCounter.increment()
        monitoring.counter(s"$name-persisted-${utils.Inference.getSimpleName(event.getClass.getName)}").increment()
        handler()
      }

  }
}
