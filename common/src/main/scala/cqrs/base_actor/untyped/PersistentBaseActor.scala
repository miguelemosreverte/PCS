package cqrs.base_actor.untyped

import akka.persistence.journal.Tagged
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import cqrs.untyped.event.{EventBus, SyncEventBus}
import ddd.AbstractState
import design_principles.actor_model.{Command, Event, Query}
import monitoring.Monitoring

import scala.reflect.ClassTag
import scala.util.Try

abstract class PersistentBaseActor[E <: Event: ClassTag, State <: AbstractState[E]: ClassTag](monitoring: Monitoring)
    extends BaseActor[E, State](monitoring)
    with PersistentActor {

  object PersistentBaseActorMonitoring {
    val persisted = monitoring.counter(s"$name-persisted")
  }

  val eventBus: EventBus[Try] = new SyncEventBus(logger)

  override def receive: Receive = super[PersistentActor].receive
  override def receiveCommand: Receive = {
    case cmd: Command =>
      commandBus.publish(cmd)
    case query: Query =>
      queryBus.ask(query)
    case other =>
      logger.warn(s"[${persistenceId}]Unexpected message $other")
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

  def persistEvent(event: E, tags: Set[String] = Set.empty)(handler: () => Unit = () => ()): Unit =
    persist(if (tags.nonEmpty) Tagged(event, tags) else event) { _ =>
      logger.debug(s"[$persistenceId] Persist event | $event")
      PersistentBaseActorMonitoring.persisted.increment()
      monitoring.counter(s"$name-persisted-${utils.Inference.getSimpleName(event.getClass.getName)}").increment()
      handler()
    }
}