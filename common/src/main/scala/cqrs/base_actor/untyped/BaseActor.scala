package cqrs.base_actor.untyped

import akka.actor.Actor
import cqrs.untyped.command.{CommandBus, SyncCommandBus}
import cqrs.untyped.query.{QueryBus, SyncQueryBus}
import ddd.AbstractState
import design_principles.actor_model.{Command, Event, Query}
import monitoring.Monitoring
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContextExecutor
import scala.util.Try

abstract class BaseActor[E <: Event, State <: AbstractState[E]](monitoring: Monitoring) extends Actor {
  val name = utils.Inference.getSimpleName(this.getClass.getName)

  object BaseActorMonitoring {
    val commands = monitoring.counter(s"$name-commands")
    val queries = monitoring.counter(s"$name-queries")
    val unexpected = monitoring.counter(s"$name-unexpected")
  }

  implicit private val ec: ExecutionContextExecutor = context.system.dispatcher
  implicit val logger: Logger = LoggerFactory.getLogger(this.getClass.getSimpleName)

  val persistenceId: String = self.path.name
  var state: State
  var lastDeliveryId: BigInt = 0

  val commandBus: CommandBus[Try] = new SyncCommandBus(logger)
  val queryBus: QueryBus[Try] = new SyncQueryBus(logger)

  def receive: Receive = {
    case cmd: Command =>
      BaseActorMonitoring.commands.increment()
      commandBus.publish(cmd)
    case query: Query =>
      BaseActorMonitoring.queries.increment()
      queryBus.ask(query)
    case unexpected =>
      BaseActorMonitoring.unexpected.increment()
      logger.warn(s"[${persistenceId}]Unexpected message $unexpected")
  }
  override def preStart(): Unit = {
    super.preStart()
    setupHandlers()
  }
  def setupHandlers(): Unit
}
