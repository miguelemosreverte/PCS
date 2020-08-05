package registrales.exencion

import akka.actor.{ActorRef, ActorSystem}
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ExencionMessageRoot
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.json._
import consumers_spec.no_registrales.testkit.MessageTestkitUtils
import consumers_spec.no_registrales.testkit.mocks.CotitularidadActorWithMockPersistence
import design_principles.external_pub_sub.kafka.{KafkaMock, MessageProcessorLogging}
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import kafka.{MessageProcessor, MessageProducer}
import monitoring.{DummyMonitoring, Monitoring}
import readside.proyectionists.registrales.exencion.ExencionProjectionHandler
import registrales.exencion.testkit.query.ExencionQueryTestkitAgainstActors

trait ExencionTestSuiteMock extends ExencionSpec {

  def testContext()(implicit system: ActorSystem): TestContext = new ExencionMockE2ETestContext()

  class ExencionMockE2ETestContext(implicit system: ActorSystem) extends BaseE2ETestContext {

    import system.dispatcher

    val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock({
      case e: ObjetoEvents.ObjetoAddedExencion =>
        (
          ExencionMessageRoot(
            e.sujetoId,
            e.objetoId,
            e.tipoObjeto,
            e.exencion.BEX_EXE_ID
          ).toString,
          serialization encode e
        )
    })

    override val cassandraRead: CassandraRead = cassandraTestkit.cassandraRead
    override val cassandraWrite: CassandraWrite = cassandraTestkit.cassandraWrite

    lazy val kafkaMock: KafkaMock = new KafkaMock()

    override def messageProducer: MessageProducer = kafkaMock

    override def messageProcessor: MessageProcessor with MessageProcessorLogging = kafkaMock

    lazy val exencionProyectionist: ExencionProjectionHandler =
      new readside.proyectionists.registrales.exencion.ExencionProjectionHandler(new DummyMonitoring) {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }

    val monitoring: Monitoring = new DummyMonitoring
    lazy val sujeto: ActorRef =
      new registrales.exencion.testkit.mocks.SujetoActorWithMockPersistence(
        exencionProyectionist
      ).startWithRequirements(monitoring)

    lazy val cotitularidadActor: ActorRef =
      new CotitularidadActorWithMockPersistence(messageProducer).startWithRequirements(monitoring)

    lazy val Query = ExencionQueryTestkitAgainstActors(sujeto)

    lazy val MessageProducers = new MessageTestkitUtils(sujeto, cotitularidadActor, messageProducer)

    def close(): Unit = {
      MessageProducers.StartMessageProcessor(messageProcessor).startProcessing()
    }
  }
}
