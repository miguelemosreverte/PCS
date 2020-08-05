package generators.consumers.no_registrales

import akka.Done
import akka.actor.ActorSystem
import akka.event.Logging
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import com.typesafe.config.ConfigFactory
import cqrs.BasePersistentShardedTypedActor
import generators.consumers.no_registrales.Generator.KafkaKeyValue
import generators.consumers.no_registrales.obligacion.{ObligacionesAntGenerator, ObligacionesTriGenerator}
import generators.consumers.no_registrales.sujeto.{SujetoAntGenerator, SujetoTriGenerator}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future
import scala.concurrent.duration._

object UserEventProducer {

  def main(args: Array[String]): Unit = {

    def isInt(s: String): Boolean = s.matches("""\d+""")

    args.toList match {
      case topic :: from :: to :: Nil if isInt(from) && isInt(to) =>
        produce(topic, from.toInt, to.toInt)
      case _ =>
        throw new IllegalArgumentException("usage: <topic> <from> <to> -- example: DGR-COP-ACTIVIDADES 1 1000")
    }
  }

  def produce(topic: String, From: Int, To: Int): Unit = {

    implicit val system: ActorSystem = ActorSystem(
      "UserEventProducer",
      ConfigFactory.parseString("""
      akka.actor.provider = "local" 
     """.stripMargin).withFallback(ConfigFactory.load()).resolve()
    )

    val log = Logging(system, "UserEventProducer")

    val config = system.settings.config.getConfig("akka.kafka.producer")

    val producerSettings: ProducerSettings[String, String] =
      ProducerSettings(config, new StringSerializer, new StringSerializer)
        .withBootstrapServers("0.0.0.0:9092")

    val generator: Generator[_] = topic match {
      case "DGR-COP-OBLIGACIONES-TRI" => new ObligacionesTriGenerator()
      case "DGR-COP-OBLIGACIONES-ANT" => new ObligacionesAntGenerator()
      case "DGR-COP-SUJETO-TRI" => new SujetoTriGenerator()
      case "DGR-COP-SUJETO-ANT" => new SujetoAntGenerator()
    }

    def producerRecord(keyValue: KafkaKeyValue): ProducerRecord[String, String] = {
      val entityId = keyValue.key
      val message = keyValue.value
      println(entityId)
      val shardAndPartition = BasePersistentShardedTypedActor.shardAndPartition(entityId)
      new ProducerRecord[String, String](topic, shardAndPartition, entityId, message)
    }

    val done: Future[Done] =
      akka.stream.scaladsl.Source
        .fromIterator[Int](() => (From to To).iterator)
        .throttle(1, 0.1 seconds)
        .map(i => generator.nextKafkaKeyValue(i))
        .map(producerRecord)
        .runWith(Producer.plainSink(producerSettings))

  }
}
