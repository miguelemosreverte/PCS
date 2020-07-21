package cqrs.typed.command

import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import design_principles.actor_model.Command

import scala.reflect.ClassTag

trait CommandBus[Event, State] {

  type AkkaEffect = Effect[Event, State]

  def publish[C <: Command, Response](command: C)(replyTo: ActorRef[akka.Done]): AkkaEffect

  def subscribe[C <: Command: ClassTag](handler: C => ActorRef[akka.Done] => AkkaEffect): Unit

}
