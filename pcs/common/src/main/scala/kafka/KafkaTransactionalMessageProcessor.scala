package kafka

import scala.concurrent.Future
import scala.util.{Failure, Success}

import akka.Done
import akka.kafka.scaladsl.Transactional
import akka.kafka.{ConsumerMessage, ProducerMessage, Subscriptions}
import akka.stream.KillSwitches
import akka.stream.scaladsl.{Keep, Sink}
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory

class KafkaTransactionalMessageProcessor()(
    implicit
    transactionRequirements: KafkaMessageProcessorRequirements
) extends MessageProcessor {

  override type KillSwitch = akka.stream.UniqueKillSwitch

  def run(
      SOURCE_TOPIC: String,
      SINK_TOPIC: String,
      algorithm: String => Future[Seq[String]]
  ): (KillSwitch, Future[Done]) = {

    type Msg = ConsumerMessage.TransactionalMessage[String, String]

    implicit val system = transactionRequirements.system
    val consumer = transactionRequirements.consumer
    val producer = transactionRequirements.producer
    import scala.concurrent.duration._

    import system.dispatcher

    var message: Msg = null

    log.info(s"Running Transaction from SOURCE_TOPIC: ${SOURCE_TOPIC} to SINK_TOPIC: ${SINK_TOPIC}")

    val stream = Transactional
      .source(consumer, Subscriptions.topics(SOURCE_TOPIC))
      .throttle(2000, 500 millis)
      .mapAsync(1) { msg: ConsumerMessage.TransactionalMessage[String, String] =>
        message = msg

        val input: String = message.record.value

        log.info(message.record.value)

        algorithm(input)
          .map { a: Seq[String] =>
            Right(a)
          }
          .recover {
            case e: Exception =>
              Left(s"""
                  Error in flow:
                  ${e.getMessage}
                  For input:
                  ${input}
              """)
          }
      }
      .map {

        case Left(cause) =>
          log.error(cause)
          val output = Seq(message.record.value)
          ProducerMessage.multi(
            records = output.map { o =>
              new ProducerRecord(
                SOURCE_TOPIC + "_retry",
                message.record.key,
                o
              )
            }.toList,
            passThrough = message.partitionOffset
          )
        case Right(output) =>
          ProducerMessage.multi(
            records = output.map { o =>
              new ProducerRecord(
                SINK_TOPIC,
                message.record.key,
                o
              )
            }.toList,
            passThrough = message.partitionOffset
          )
      }
      .via(Transactional.flow(producer, transactionalId))
      .viaMat(KillSwitches.single)(Keep.right)
      .collect {
        case a: ProducerMessage.MultiResult[_, String, _] =>
          a.parts.map(a => a.record.value)
      }
      .withAttributes(akka.defaultSupervisionStrategy)
      .toMat(Sink.ignore)(Keep.both)

    val (killSwitch, done) = stream.run()

    done.onComplete {
      case Success(value) =>
        log.info(s"Stream completed with success -- ${value}")
      // run(SOURCE_TOPIC,SINK_TOPIC,algorithm)
      case Failure(ex) =>
        log.error(s"Stream completed with failure -- ${ex.getMessage}")
      // run(SOURCE_TOPIC,SINK_TOPIC,algorithm)
    }

    (killSwitch, done)
  }

  def transactionalId: String = java.util.UUID.randomUUID().toString

  val log = LoggerFactory.getLogger(this.getClass)
}
