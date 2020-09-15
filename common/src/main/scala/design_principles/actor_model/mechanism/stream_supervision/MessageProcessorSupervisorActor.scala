package design_principles.actor_model.mechanism.stream_supervision

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.stream.KillSwitch
import api.actor_transaction.ActorTransactionController
import design_principles.actor_model.mechanism.stream_supervision.StartStopSingleton.{
  `hey, i just can't stop thinking about you!`,
  `hey, i just meet you, and this is crazy, but here's my number, so call me maybe`,
  `me neither`,
  Start,
  Stop
}

class MessageProcessorSupervisorActor(
    startStopSingleton: ActorRef,
    streams: Set[ActorTransactionController]
) extends Actor
    with ActorLogging {

  var killSwitches: Set[Option[KillSwitch]] = Set.empty

  override def preStart(): Unit = {
    super.preStart()
    startStopSingleton ! `hey, i just meet you, and this is crazy, but here's my number, so call me maybe`(
      self
    )
  }

  override def receive: Receive = {

    case `hey, i just can't stop thinking about you!`() => sender() ! `me neither`()

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
