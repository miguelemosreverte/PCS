package design_principles.actor_model.mechanism.stream_supervision

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.stream.KillSwitch
import api.actor_transaction.ActorTransactionController
import design_principles.actor_model.mechanism.stream_supervision.StartStopSingleton.{
  Ping,
  Pong,
  Start,
  StartByTopic,
  Stop,
  StopByTopic,
  SubscribeMe
}

import scala.collection.mutable

class MessageProcessorSupervisorActor(
    startStopSingleton: ActorRef,
    streams: Map[String, ActorTransactionController]
) extends Actor
    with ActorLogging {

  var killSwitches: mutable.Map[String, KillSwitch] = mutable.Map.empty

  override def preStart(): Unit = {
    super.preStart()
    streams.foreach {
      case (topic, controller) =>
        startStopSingleton ! SubscribeMe(
          topic,
          self
        )
    }
  }

  override def receive: Receive = {

    case Ping() => sender() ! Pong()

    case StartByTopic(topic) =>
      if (killSwitches.contains(topic)) {
        log.info(s"Already started!")
      } else {
        streams(topic).startTransaction() foreach { killSwitch =>
          killSwitches(topic) = killSwitch
        }
        log.info(s"Starting all kafka consumers on node")
      }

    case StopByTopic(topic) =>
      if (!killSwitches.contains(topic)) {
        log.info(s"There are no kafka consumers to stop on node")
      } else {
        log.info(s"Stopping all kafka consumers on node ")
        killSwitches(topic).shutdown
        killSwitches.remove(topic)
      }

    case Start() =>
      if (killSwitches.nonEmpty) {
        log.info(s"""
        This API is for global initialization of all streams.
        Some streams are already up and running. 
        If you want to start all of them,
        hit the /kafka/stop API first, 
        and then hit this API again.
        """)
      } else {
        killSwitches = mutable.Map(streams.flatMap {
          case (topic, stream) =>
            val killswitch = stream.startTransaction()
            killswitch.map(
              (topic, _)
            )
        }.toSeq: _*)
        log.info(s"Starting all kafka consumers on node")
      }

    case Stop() =>
      if (killSwitches.isEmpty) {
        log.info(s"There are no kafka consumers to stop on node")
      } else {
        log.info(s"Stopping all kafka consumers on node ")
        killSwitches.values foreach (_.shutdown)
        killSwitches = killSwitches.empty
      }
  }
}
