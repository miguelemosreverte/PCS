package kafka

import scala.concurrent.Future
import scala.util.{Failure, Success}

import akka.Done
import akka.kafka.scaladsl.{Committer, Consumer, Producer}
import akka.kafka.{CommitterSettings, ConsumerMessage, ProducerMessage, Subscriptions}
import akka.stream.KillSwitches
import akka.stream.scaladsl.Keep
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory

class KafkaCommitableMessageProcessor()(
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

    log.info(s"Running Transaction from SOURCE_TOPIC: ${SOURCE_TOPIC} to SINK_TOPIC: ${SINK_TOPIC}")

    type Message = ConsumerMessage.CommittableMessage[String, String]

    val stream = Consumer
      .committableSource(consumer, Subscriptions.topics(SOURCE_TOPIC))
      .throttle(40000, 1 second)
      .mapAsync(1) { message: Message =>
        val input: String = message.record.value

        //this.logger.info(message)(LoggableTransaction)
        log.debug(message.record.value)

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
          .map((message, _))
      }
      .map {

        case (message: Message, Left(cause: String)) =>
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
            passThrough = message.committableOffset
          )
        case (message: Message, Right(output: Seq[String])) =>
          ProducerMessage.multi(
            records = output.map { o =>
              new ProducerRecord(
                SINK_TOPIC,
                message.record.key,
                o
              )
            }.toList,
            passThrough = message.committableOffset
          )
      }
      .via(Producer.flexiFlow(producer))
      .map(_.passThrough)
      .viaMat(KillSwitches.single)(Keep.right)
      .toMat(Committer.sink(CommitterSettings(system)))(Keep.both)
      //.mapMaterializedValue(DrainingControl.apply)
      .withAttributes(akka.defaultSupervisionStrategy)

    val (killSwitch, done) = stream.run()

    done.onComplete {
      case Success(value) =>
        log.warn(s"Stream completed with success -- ${value}")
      case Failure(ex) =>
        log.error(s"Stream completed with failure -- ${ex.getMessage}")
    }

    (killSwitch, done)
  }

  def transactionalId: String = java.util.UUID.randomUUID().toString

  val log = LoggerFactory.getLogger(this.getClass)
}
