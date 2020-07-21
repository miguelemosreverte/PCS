package design_principles.actor_model

import java.net.ServerSocket

import scala.concurrent.duration._
import scala.util.Try

import akka.actor.ActorSystem
import akka.pattern.ask
import design_principles.actor_model.system_parallelizable.ActorSystemParallelizer.RunTest
import design_principles.actor_model.system_parallelizable.ActorSystemParallelizerBuilder
import design_principles.actor_model.utils.Generators
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

// @TODO Remove
object ActorSpec {
  def system: ActorSystem = {
    val availablePort = new ServerSocket(0).getLocalPort
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
    with ScalaFutures {

  def parallelActorSystemRunner(testContext: ActorSystem => Unit): Unit =
    ActorSystemParallelizerBuilder.actor
      .ask(RunTest(testContext))(2.minutes)
      .mapTo[Try[Unit]]
      .futureValue(timeout(2.minutes))
      .get
}
