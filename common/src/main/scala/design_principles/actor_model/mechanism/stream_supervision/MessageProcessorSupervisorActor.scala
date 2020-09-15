package design_principles.actor_model.mechanism.stream_supervision

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.stream.KillSwitch
import api.actor_transaction.ActorTransactionController
import design_principles.actor_model.mechanism.stream_supervision.StartStopSingleton.{
  Ping,
  Pong,
  Start,
  Stop,
  SubscribeMe
}

class MessageProcessorSupervisorActor(
    startStopSingleton: ActorRef,
    streams: Set[ActorTransactionController]
) extends Actor
    with ActorLogging {

  var killSwitches: Set[Option[KillSwitch]] = Set.empty

  override def preStart(): Unit = {
    super.preStart()
    startStopSingleton ! SubscribeMe(
      self
    )
  }

  override def receive: Receive = {

    case Ping() => sender() ! Pong()

    case Start() =>
      if (killSwitches.nonEmpty) {
        log.info(s"Already started!")
      } else {
        killSwitches = streams.map(_.startTransaction())
        log.info(s"Starting all kafka consumers on node")
      }

    case Stop() =>
      if (killSwitches.isEmpty) {
        log.info(s"There are no kafka consumers to stop on node")
      } else {
        log.info(s"Stopping all kafka consumers on node ")
        killSwitches map (_ map (_.shutdown))
        killSwitches = killSwitches.empty
      }
  }
}
