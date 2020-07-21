package no_registrales

import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import consumers_spec.no_registrales.testkit.query.{NoRegistralesQueryTestKit, NoRegistralesQueryWithHTTP}
import design_principles.actor_model.testkit.KafkaTestkit
import design_principles.actor_model.testkit.QueryTestkit.AgainstHTTP
import design_principles.external_pub_sub.kafka.MessageProcessorLogging
import design_principles.projection.infrastructure.CassandraTestkitProduction
import kafka.{MessageProcessor, MessageProducer}

trait NoRegistralesTestSuiteE2E extends BaseE2ESpec {

  def testContext()(implicit system: ActorSystem): TestContext = new E2ETestContext()

  class E2ETestContext(implicit system: ActorSystem) extends BaseE2ETestContext {

    import system.dispatcher

    val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction()

    override val cassandraRead: CassandraRead = cassandraTestkit.cassandraRead
    override val cassandraWrite: CassandraWrite = cassandraTestkit.cassandraWrite

    val kafka = new KafkaTestkit()
    override def messageProducer: MessageProducer = kafka.messageProducer
    override def messageProcessor: MessageProcessor with MessageProcessorLogging = kafka.messageProcessor

    val Query: NoRegistralesQueryTestKit with AgainstHTTP = new NoRegistralesQueryWithHTTP()

    def close(): Unit = ()
  }
}
