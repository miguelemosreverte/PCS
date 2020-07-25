package kafka

import scala.concurrent.Future

import akka.Done

/*
This mechanism allows the user to process messages from the message bus

It delegates onto the user the deserialization of the messages

It returns a tuple which contains

1. KillSwitch for the user to stop the message stream
2. Future[Done] to handle the correct stream termination
 */
trait MessageProcessor {

  type KillSwitch
  def run(
      SOURCE_TOPIC: String,
      SINK_TOPIC: String,
      algorithm: String => Future[Seq[String]]
  ): (KillSwitch, Future[Done])

}
