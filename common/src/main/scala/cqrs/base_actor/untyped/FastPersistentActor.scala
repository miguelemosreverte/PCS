package cqrs.base_actor.untyped

import akka.Done
import akka.actor.{ActorContext, ActorRef, ActorSystem, Props}
import akka.persistence.PersistentActor
import cqrs.base_actor.untyped.SaveToCassandraActor.{getSaveToCassandraActor, session, statement, SerializedEvent}

trait FastPersistentActor { a: PersistentActor =>
  val s: ActorSystem = a.context.system

  val persistenceId: String
  private final val aggregateRoot = persistenceId

  private implicit val ec = s.dispatcher

  def fastPersist[A](event: A)(handler: A => Unit): Unit = {
    println("""
        |
        | CALLING FAST PERSIST
        |
        |""".stripMargin)
    val serializedEvent = event.toString // serialization.encode(event)

    session
      .executeAsync(statement.bind(aggregateRoot, serializedEvent))
      .toScala
      .map { _ =>
        Done
      } pipeTo (self)

    getSaveToCassandraActor(context.system) ! SerializedEvent(
      aggregateRoot,
      serializedEvent
    )
    handler(event)
  }
}
