package cqrs

import akka.actor.Actor
import cqrs.untyped.command.{CommandBus, SyncCommandBus}
import cqrs.untyped.query.{QueryBus, SyncQueryBus}
import ddd.AbstractState
import design_principles.actor_model.{Command, Event, Query}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContextExecutor
import scala.util.Try

abstract class BaseActor[E <: Event, State <: AbstractState[E]] extends Actor {

  implicit private val ec: ExecutionContextExecutor = context.system.dispatcher
  implicit val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

  val persistenceId: String = self.path.name
  var state: State
  var lastDeliveryId: BigInt = 0

  val commandBus: CommandBus[Try] = new SyncCommandBus(logger)
  val queryBus: QueryBus[Try] = new SyncQueryBus(logger)

  def receive: Receive = {
    case cmd: Command =>
      commandBus.publish(cmd)
    case query: Query =>
      queryBus.ask(query)
    case other =>
      logger.warn(s"[${persistenceId}]Unexpected message $other")
  }
  override def preStart(): Unit = {
    super.preStart()
    setupHandlers()
  }
  def setupHandlers(): Unit
}
