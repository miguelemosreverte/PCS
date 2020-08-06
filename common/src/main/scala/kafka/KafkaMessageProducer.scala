package kafka

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import monitoring.Monitoring
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.{Logger, LoggerFactory}

class KafkaMessageProducer()(
    implicit
    system: ActorSystem,
    producerSettings: ProducerSettings[String, String]
) extends MessageProducer {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def produce(data: Seq[String], topic: String)(handler: Seq[String] => Unit): Future[Done] = {

    implicit val ec: ExecutionContextExecutor = system.getDispatcher

    val publication: Future[Done] = Source(data)
    // NOTE: If no partition is specified but a key is present a partition will be chosen
    // using a hash of the key. If neither key nor partition is present a partition
    // will be assigned in a round-robin fashion.
      .map(new ProducerRecord[String, String](topic, _))
      .runWith(Producer.plainSink(producerSettings))

    publication.onComplete {
      case Success(Done) =>
        data foreach { s =>
          log.debug(s"""Published $s to $topic""")
        }

        handler(data)
      case Failure(t) => log.error("An error has occurred: " + t.getMessage)
    }

    publication

  }
}

object KafkaMessageProducer {

  def apply(monitoring: Monitoring,
            rebalancerListener: ActorRef)(implicit system: ActorSystem): KafkaMessageProducer = {
    implicit def kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements =
      KafkaMessageProcessorRequirements.productionSettings(Some(rebalancerListener), monitoring, system)
    implicit def producerSettings: ProducerSettings[String, String] = kafkaMessageProcessorRequirements.producer
    new KafkaMessageProducer()
  }
}
