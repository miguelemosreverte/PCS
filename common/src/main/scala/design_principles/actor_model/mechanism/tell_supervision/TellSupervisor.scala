package design_principles.actor_model.mechanism.tell_supervision

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import design_principles.actor_model.Command
import design_principles.actor_model.Response.SuccessProcessing

import scala.collection.mutable
import java.time.Duration
import java.time.temporal.ChronoUnit

import scala.concurrent.duration.DurationInt

object TellSupervisor {
  private def props(actorRef: ActorRef): Props = Props(new TellSupervisor(actorRef))
  def start(actorRef: ActorRef)(implicit system: ActorSystem): ActorRef = system.actorOf(props(actorRef))
}

class TellSupervisor(actorRef: ActorRef) extends Actor {
  var commands: mutable.Map[String, mutable.Map[BigInt, Command]] = mutable.Map.empty
  override def receive: Receive = {
    case SuccessProcessing(aggregateRoot, deliveryId) =>
      commands.get(aggregateRoot) map { _ =>
        commands(aggregateRoot).remove(deliveryId)
        if (commands(aggregateRoot).isEmpty) commands.remove(aggregateRoot)
      }
    case command: Command =>
      commands.get(command.aggregateRoot) match {
        case None => commands(command.aggregateRoot) = mutable.Map.empty
        case Some(_) =>
      }
      commands(command.aggregateRoot)(command.deliveryId) = command
      actorRef ! command
      sender() ! SuccessProcessing(command.aggregateRoot, command.deliveryId)
  }
  class Resend extends Runnable {
    override def run(): Unit =
      commands.foreach {
        case (aggregateRoot: String, commandMap: mutable.Map[BigInt, Command]) =>
          commandMap foreach {
            case (deliveryId: BigInt, command: Command) =>
              actorRef ! command
          }
      }
  }

  context.system.scheduler.scheduleAtFixedRate(10.seconds, 20.seconds) { () =>
    commands.foreach {
      case (aggregateRoot: String, commandMap: mutable.Map[BigInt, Command]) =>
        commandMap foreach {
          case (deliveryId: BigInt, command: Command) =>
            actorRef ! command
        }
    }
  }(context.system.dispatcher)
}
