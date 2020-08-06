package registrales.exencion

import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import design_principles.actor_model.testkit.KafkaTestkit
import design_principles.actor_model.testkit.QueryTestkit.AgainstHTTP
import design_principles.external_pub_sub.kafka.MessageProcessorLogging
import design_principles.projection.infrastructure.CassandraTestkitProduction
import kafka.{MessageProcessor, MessageProducer}
import monitoring.DummyMonitoring
import registrales.exencion.testkit.query.{ExencionQueryTestkit, ExencionQueryTestkitAgainstHTTP}

trait ExencionTestSuiteE2E extends ExencionSpec {

  def testContext()(implicit system: ActorSystem): TestContext = new ExencionE2ETestContext()

  class ExencionE2ETestContext(implicit system: ActorSystem) extends BaseE2ETestContext {

    implicit val ec = system.dispatcher
    val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction()

    override val cassandraRead: CassandraRead = cassandraTestkit.cassandraRead
    override val cassandraWrite: CassandraWrite = cassandraTestkit.cassandraWrite

    val kafka = new KafkaTestkit(new DummyMonitoring)
    override def messageProducer: MessageProducer = kafka.messageProducer
    override def messageProcessor: MessageProcessor with MessageProcessorLogging = kafka.messageProcessor

    val Query: ExencionQueryTestkit with AgainstHTTP = new ExencionQueryTestkitAgainstHTTP()

    def close(): Unit = ()
  }
}
