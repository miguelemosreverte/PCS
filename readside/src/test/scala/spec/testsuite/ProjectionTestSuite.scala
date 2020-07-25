package spec.testsuite

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import design_principles.actor_model.ActorSpec
import serialization.EventSerializer

trait ProjectionTestSuite[Events, AggregateRoot] extends ActorSpec {
  override lazy val actorConfig: Config = ProjectionTestSuite.config

  def testContext()(implicit system: ActorSystem): ProjectionTestContext[Events, AggregateRoot]

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    println(s"${Console.YELLOW}Starting tests for ${this.getClass.getName}${Console.RESET}".stripMargin)
  }
}

object ProjectionTestSuite {
  private val overrideConfig: Config =
    ConfigFactory.parseString("""
      akka.loglevel = INFO
      #akka.persistence.typed.log-stashing = on
      akka.actor.provider = cluster
      #akka.remote.classic.netty.tcp.port = 0
      #akka.remote.artery.canonical.port = 0
      akka.remote.artery.canonical.hostname = 127.0.0.1
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.journal.inmem.test-serialization = on
      akka.actor.allow-java-serialization = true
      akka.cluster.jmx.multi-mbeans-in-same-jvm = on
    """)

  private val config: Config = Seq(
    ConfigFactory parseString EventSerializer.eventAdapterConf,
    ConfigFactory parseString EventSerializer.serializationConf,
    overrideConfig,
    ConfigFactory.load()
  ).reduce(_ withFallback _)
}
