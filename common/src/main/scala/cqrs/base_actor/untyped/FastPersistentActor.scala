package cqrs.base_actor.untyped

import akka.Done
import akka.actor.{ActorContext, ActorRef, ActorSystem, Props}
import akka.persistence.PersistentActor
import cqrs.base_actor.untyped.SaveToCassandraActor.SerializedEvent

trait FastPersistentActor { a: PersistentActor =>
  val s: ActorSystem = a.context.system

  val saveToCassandraActor: ActorRef = s.actorOf(Props(new SaveToCassandraActor()))
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

    saveToCassandraActor ! SerializedEvent(
      aggregateRoot,
      serializedEvent
    )
  }
}
