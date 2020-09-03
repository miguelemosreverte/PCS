package generator

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import akka.Done
import akka.actor.ActorSystem
import akka.event.Logging
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import com.typesafe.config.ConfigFactory
import generator.Generator.KafkaKeyValue
import no_registrales.obligacion.{ObligacionesAntGenerator, ObligacionesTriGenerator}
import no_registrales.sujeto.{SujetoAntGenerator, SujetoTriGenerator}
import kafka.KafkaMessageShardProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import registrales.actividad_sujeto.ActividadSujetoGenerator

object KafkaEventProducer {

  def main(args: Array[String]): Unit = {

    def isInt(s: String): Boolean = s.matches("""\d+""")

    args.toList match {
      case kafkaServer :: topic :: from :: to :: Nil if isInt(from) && isInt(to) =>
        produce(kafkaServer, topic, from.toInt, to.toInt)
      case _ =>
        throw new IllegalArgumentException("usage: <topic> <from> <to> -- example: DGR-COP-ACTIVIDADES 1 1000")
    }
  }

  def produce(kafkaServer: String = "0.0.0.0:9092", topic: String, From: Int, To: Int): Unit = {

    implicit val system: ActorSystem = ActorSystem(
      "KafkaEventProducer",
      ConfigFactory.parseString("""
      akka.actor.provider = "local" 
     """.stripMargin).withFallback(ConfigFactory.load()).resolve()
    )

    val log = Logging(system, "KafkaEventProducer")

    val config = system.settings.config.getConfig("akka.kafka.producer")

    val producerSettings: ProducerSettings[String, String] =
      ProducerSettings(config, new StringSerializer, new StringSerializer)
        .withBootstrapServers(kafkaServer)

    val generator: Generator[_] = topic match {
      case "DGR-COP-OBLIGACIONES-TRI" => new ObligacionesTriGenerator()
      case "DGR-COP-OBLIGACIONES-ANT" => new ObligacionesAntGenerator()
      case "DGR-COP-SUJETO-TRI" => new SujetoTriGenerator()
      case "DGR-COP-SUJETO-ANT" => new SujetoAntGenerator()
      case "DGR-COP-ACTIVIDADES" => new ActividadSujetoGenerator()
    }

    def produce(keyValue: ((Int, KafkaKeyValue))) = {
      if (keyValue._1 % 100000 == 0) println(keyValue)
      KafkaMessageShardProducerRecord.producerRecord(topic, 20, keyValue._2.aggregateRoot, keyValue._2.json)
    }

    val done: Future[Done] =
      akka.stream.scaladsl.Source
        .fromIterator[Int](() => (From to To).iterator)
        // .throttle(1, 0.1 seconds)
        .map(i => (i, generator.nextKafkaKeyValue(i)))
        .map(produce)
        .runWith(Producer.plainSink(producerSettings))

    done.onComplete {
      case Success(value) =>
        log.info(s"KakfaEventProducer finished with Success($value)")
        System.exit(0)
      case Failure(exception) =>
        log.info(s"KakfaEventProducer finished with Failure($exception)")
        System.exit(1)
    }(system.dispatcher)
  }
}
