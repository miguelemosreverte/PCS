package utils

import akka.Done
import akka.kafka.ProducerSettings
import akka.kafka.testkit.internal.TestFrameworkInterface
import akka.kafka.testkit.scaladsl.{EmbeddedKafkaLike, KafkaSpec}
import akka.util.Timeout
import design_principles.actor_model.ActorSpec
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Format
import utils.generators.Numbers._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.reflect.ClassTag

import akka.actor.ActorSystem

abstract class KafkaTestKitBaseSpec
    extends KafkaSpec(
      nextKafkaPort,
      zooKeeperPort = nextKafkaPort,
      actorSystem = ActorSystem("") // ActorSpec.system
    )
    with EmbeddedKafkaLike
    with AnyFlatSpecLike
    with TestFrameworkInterface.Scalatest
    with Matchers
    with BeforeAndAfter
    with ScalaFutures
    with Eventually
    with IntegrationPatience {

  implicit val timeout: Timeout = 20 seconds
  implicit val producer: ProducerSettings[String, String] = producerDefaults

  implicit override val ec: ExecutionContextExecutor = system.dispatcher

  def produceMessage(topic: String, message: String): Future[Done] =
    produceString(topic, Seq(message))

  def expectTypedMessage[A: ClassTag](topic: String)(implicit format: Format[A]): A = {
    import serialization.decodeF
    decodeF[A](
      createProbe(consumerDefaults, topic)._2
        .requestNext()
    )
  }
  def expectMessage(topic: String): String =
    createProbe(consumerDefaults, topic)._2
      .requestNext()

  override def cleanUp(): Unit = {
    //testProducer.close(60, TimeUnit.SECONDS)
    //cleanUpAdminClient()
  }
}
