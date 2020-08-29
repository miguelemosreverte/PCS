package consumers_spec.no_registrales.testsuite

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.ShardedEntityRequirements
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import consumers.no_registral.objeto.infrastructure.json._
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionPersistedSnapshot
import consumers.no_registral.obligacion.infrastructure.json._
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoSnapshotPersisted
import consumers.no_registral.sujeto.infrastructure.json._
import consumers_spec.no_registrales.testkit.MessageTestkitUtils
import consumers_spec.no_registrales.testkit.mocks.{
  CotitularidadActorWithMockPersistence,
  SujetoActorWithMockPersistence
}
import consumers_spec.no_registrales.testkit.query.{NoRegistralesQueryTestKit, NoRegistralesQueryWithActorRef}
import design_principles.actor_model.testkit.QueryTestkit.AgainstActors
import design_principles.external_pub_sub.kafka.{KafkaMock, MessageProcessorLogging}
import design_principles.projection.mock.CassandraTestkitMock
import kafka.{MessageProcessor, MessageProducer}
import monitoring.DummyMonitoring

trait NoRegistralesTestSuiteMock extends NoRegistralesTestSuite {
  def testContext()(implicit system: ActorSystem): TestContext = new MockTestContext()

  class MockTestContext(implicit system: ActorSystem) extends TestContext {

    implicit val shardedEntityRequirements: ShardedEntityRequirements = ShardedEntityRequirements(
      system
    )

    val monitoring = new DummyMonitoring
    val sujeto: ActorRef = new SujetoActorWithMockPersistence(messageProducer).start
    val cotitularidadActor: ActorRef =
      new CotitularidadActorWithMockPersistence(messageProducer).startWithRequirements(monitoring)

    val cassandraTestkit = new CassandraTestkitMock({
      case e: ObjetoSnapshotPersisted =>
        (
          ObjetoMessageRoots(e.sujetoId, e.objetoId, e.tipoObjeto).toString,
          serialization encode e
        )
      case e: ObligacionPersistedSnapshot =>
        (
          ObligacionMessageRoots(e.sujetoId, e.objetoId, e.tipoObjeto, e.obligacionId).toString,
          serialization encode e
        )
      case e: SujetoSnapshotPersisted =>
        (
          SujetoMessageRoots(e.sujetoId).toString,
          serialization encode e
        )
    })

    lazy val kafkaMock: KafkaMock = new KafkaMock()

    override def messageProducer: MessageProducer = kafkaMock

    override def messageProcessor: MessageProcessor with MessageProcessorLogging = kafkaMock

    override val Query: NoRegistralesQueryTestKit with AgainstActors =
      new NoRegistralesQueryWithActorRef(sujeto)

    val MessageProducers = new MessageTestkitUtils(sujeto, cotitularidadActor, messageProducer)

    MessageProducers.StartMessageProcessor(messageProcessor).startProcessing()

    def close(): Unit = {}
  }
}
