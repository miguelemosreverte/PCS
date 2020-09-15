package design_principles.actor_model.mechanism.stream_supervision

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.entity.ShardedEntity.NoRequirements
import akka.entity.SingletonEntity

import scala.concurrent.duration.DurationInt

object StartStopSingleton extends SingletonEntity[NoRequirements] {
  case class SubscribeMe(actorRef: ActorRef)
  case class Start()
  case class Stop()
  case class Ping()
  case class Pong()

  override def props(requirements: NoRequirements): Props = Props(new StartStopSingleton())
  def start(implicit actorSystem: ActorSystem): ActorRef = startWithRequirements(NoRequirements())
}

class StartStopSingleton() extends Actor with ActorLogging {

  import StartStopSingleton._

  var phonebook: Seq[ActorRef] = Seq.empty

  override def receive: Receive = {

    case SubscribeMe(actorRef: ActorRef) =>
      phonebook = phonebook :+ actorRef
      log.info(s"StartStopSingleton added ${actorRef.path.name} to the list to be stopped/started")

    case Start() =>
      phonebook foreach { _ ! Start() }
    case Stop() =>
      phonebook foreach { _ ! Stop() }

    case Pong() =>
  }

  object Call extends Runnable {
    def run(): Unit = {
      phonebook foreach { _ ! Ping() }
    }
  }

  context.system.scheduler.scheduleOnce(30 seconds, Call)(context.system.dispatcher)

}
