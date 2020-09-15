package design_principles.actor_model.mechanism.stream_supervision

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.entity.ShardedEntity.NoRequirements
import akka.entity.SingletonEntity

import scala.concurrent.duration.DurationInt

object StartStopSingleton extends SingletonEntity[NoRequirements] {
  // 🎵 🎵 🎵 https://www.youtube.com/watch?v=fWNaR-rxAic 🎵 🎵 🎵
  case class `hey, i just meet you, and this is crazy, but here's my number, so call me maybe`(actorRef: ActorRef)
  case class Start()
  case class Stop()
  case class `hey, i just can't stop thinking about you!`()
  case class `me neither`()

  override def props(requirements: NoRequirements): Props = Props(new StartStopSingleton())
  def start(implicit actorSystem: ActorSystem): ActorRef = startWithRequirements(NoRequirements())
}

class StartStopSingleton() extends Actor with ActorLogging {

  import StartStopSingleton._

  var phonebook: Seq[ActorRef] = Seq.empty

  override def receive: Receive = {

    case `hey, i just meet you, and this is crazy, but here's my number, so call me maybe`(actorRef: ActorRef) =>
      phonebook = phonebook :+ actorRef
      log.info(s"StartStopSingleton added ${actorRef.path.name} to the list to be stopped/started")

    case Start() =>
      phonebook foreach { _ ! Start() }
    case Stop() =>
      phonebook foreach { _ ! Stop() }

    case `me neither`() =>
  }

  object Call extends Runnable {
    def run(): Unit = {
      phonebook foreach { _ ! `hey, i just can't stop thinking about you!`() }
    }
  }

  context.system.scheduler.scheduleOnce(30 seconds, Call)(context.system.dispatcher)

}
