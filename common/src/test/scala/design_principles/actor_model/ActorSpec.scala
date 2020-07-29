package design_principles.actor_model

import java.net.ServerSocket

import scala.concurrent.duration._
import scala.util.Try
import akka.actor.ActorSystem
import akka.pattern.ask
import com.typesafe.config.{Config, ConfigFactory}
import design_principles.actor_model.system_parallelizable.ActorSystemGenerator.RunTest
import design_principles.actor_model.system_parallelizable.ActorSystemParallelizerBuilder
import design_principles.actor_model.utils.Generators
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import serialization.EventSerializer

object ActorSpec {
  private lazy val config: Config = Seq(
    ConfigFactory parseString EventSerializer.eventAdapterConf,
    ConfigFactory parseString EventSerializer.serializationConf,
    ConfigFactory.load()
  ).reduce(_ withFallback _)

  // TODO wrap in atomically
  def system: ActorSystem = {
    val r = new scala.util.Random
    val availablePort = r.between(2600, 3000)
    //val availablePort = new ServerSocket(0).getLocalPort
    Generators.actorSystem(availablePort)
  }
}

// @TODO replace with ActorSystemParallelizerSpec
abstract class ActorSpec
    extends AnyFlatSpecLike
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with Eventually
    with IntegrationPatience
    // with RandomTestOrder
    with ScalaFutures {

  lazy val actorConfig: Config = ActorSpec.config
  def parallelActorSystemRunner(testContext: ActorSystem => Unit): Unit =
    testContext(ActorSpec.system)
  /*ActorSystemParallelizerBuilder.actor
      .ask(RunTest(actorConfig, testContext))(2.minutes)
      .mapTo[Try[Unit]]
      .futureValue(timeout(2.minutes))
      .get*/
}
