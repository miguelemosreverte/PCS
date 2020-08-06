package consumers_spec

import akka.actor.Status.Success
import akka.actor.{Actor, ActorRef, Props}
import design_principles.actor_model.{Command, Query, Response}
import org.slf4j.{Logger, LoggerFactory}

import scala.reflect.ClassTag

class MockParentActor[ParentCommands: ClassTag, ParentResponses: ClassTag](typeName: String, childProps: Props)
    extends Actor {

  val child: ActorRef = context.actorOf(childProps, typeName)
  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def receive: PartialFunction[Any, Unit] = {
    case (cmd: ParentCommands, replyTo: ActorRef) =>
      log.info(s"[MockParentActor-$typeName] We received a command from the child actor: $cmd")
      replyTo ! Success(Response.SuccessProcessing())

    case msg @ (_: ParentResponses, replyTo: ActorRef) =>
      log.info(s"[MockParentActor-$typeName] We received a response from the child actor: $msg")
      replyTo ! Success(Response.SuccessProcessing())

    case msg @ (_: Any, replyTo: ActorRef) =>
      log.info(s"[MockParentActor-$typeName] We received a Any msg from the child actor: $msg")
      replyTo ! Success(Response.SuccessProcessing())

    case cmd: Command =>
      log.info(s"[MockParentActor-$typeName] Sent message to child $cmd")
      child forward cmd

    case query: Query =>
      child forward query

    case other =>
      log.info(s"[MockParentActor-$typeName] Unexpected message: $other")
  }
}
