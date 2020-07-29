package spec.testsuite

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import design_principles.actor_model.ActorSpec
import serialization.EventSerializer

trait ProjectionTestSuite[Events, AggregateRoot] extends ActorSpec {

  def testContext()(implicit system: ActorSystem): ProjectionTestContext[Events, AggregateRoot]

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    println(s"${Console.YELLOW}Starting tests for ${this.getClass.getName}${Console.RESET}".stripMargin)
  }
}
