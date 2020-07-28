package design_principles.external_pub_sub.kafka

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorSystem
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Sink, Source, SourceQueue}
import api.actor_transaction.ActorTransaction
import kafka.{KafkaCommitableMessageProcessor, KafkaTransactionalMessageProcessor, MessageProcessor, MessageProducer}

class KafkaMock()(implicit system: ActorSystem, ec: ExecutionContext)
    extends MessageProducer
    with MessageProcessor
    with MessageProcessorLogging {

  object PubSub {
    case class Message(topic: String, message: String)
    case class SubscribeMe(topic: String, algorithm: String => Future[Seq[String]])
    case class GetMessages()
  }
  import PubSub._
  var subscriptors: Set[SubscribeMe] = Set.empty
  var queue: SourceQueue[PubSub.Message] = Source
    .queue[Message](bufferSize = 1000, OverflowStrategy.fail)
    .map {
      case Message(topic, message) =>
        println(
          s"""
            |${Console.YELLOW} [MessageProducer] ${Console.RESET}
            |Sending message to: ${subscriptors
               .filter(_.topic == topic)
               .map(_.topic)
               .map(Console.YELLOW + _ + Console.RESET)
               .mkString(",")}
            |${Console.CYAN} $message ${Console.RESET}
            |""".stripMargin
        )
        subscriptors.filter(_.topic == topic).foreach {
          _.algorithm(message)
        }
    }
    .to(Sink.ignore)
    .run()

  def receive(message: Any): Any = message match {
    case m: Message =>
      messageHistory = messageHistory :+ ((m.topic, m.message))
      queue.offer(m)
    case GetMessages =>
    case s: SubscribeMe =>
      subscriptors = subscriptors + s
  }

  override def produce(data: Seq[String], topic: String)(handler: Seq[String] => Unit): Future[Done] = {
    data map {
      PubSub.Message(topic, _)
    } foreach { receive }
    handler(data)
    Future(Done)
  }

  override type KillSwitch = Done

  override def run(SOURCE_TOPIC: String,
                   SINK_TOPIC: String,
                   algorithm: String => Future[Seq[String]]): (KillSwitch, Future[Done]) =
    (Done, {
      receive(PubSub.SubscribeMe(SOURCE_TOPIC, algorithm))
      Future(Done)
    })

}

object KafkaMock {

  implicit class MessageProcessorImplicits(messageConsumer: MessageProcessor) {
    def subscribeActorTransaction(SOURCE_TOPIC: String, actorTransaction: ActorTransaction[_])(
        implicit ec: ExecutionContext
    ): (_, Future[Done]) =
      messageConsumer match {
        case processor: KafkaCommitableMessageProcessor =>
          processor.run(SOURCE_TOPIC,
                        SOURCE_TOPIC + "_done",
                        message => actorTransaction.transaction(message).map(_ => Seq("Done")))

        case processor: KafkaTransactionalMessageProcessor =>
          processor.run(SOURCE_TOPIC,
                        SOURCE_TOPIC + "_done",
                        message => actorTransaction.transaction(message).map(_ => Seq("Done")))

        case kafkaMock: KafkaMock =>
          (Done, {
            kafkaMock.receive(
              kafkaMock.PubSub.SubscribeMe(SOURCE_TOPIC,
                                           message => actorTransaction.transaction(message).map(_ => Seq("Done")))
            )
            Future(Done)
          })
        case _ =>
          (Done, Future(Done))
      }
  }
}
