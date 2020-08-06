package kafka

import scala.concurrent.Future

import akka.Done

/*
This mechanism allows the user to publish message to the message bus

It delegates onto the user the serialization of the messages
 */

trait MessageProducer {
  def produce(data: Seq[String], topic: String)(handler: Seq[String] => Unit): Future[Done]
}
