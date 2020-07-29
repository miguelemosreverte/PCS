package no_registrales

import akka.actor.{ActorRef, ActorSystem}
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
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
import consumers_spec.no_registrales.testkit.mocks.CotitularidadActorWithMockPersistence
import consumers_spec.no_registrales.testkit.query.{NoRegistralesQueryTestKit, NoRegistralesQueryWithActorRef}
import design_principles.actor_model.testkit.QueryTestkit.AgainstActors
import design_principles.external_pub_sub.kafka.{KafkaMock, MessageProcessorLogging}
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import kafka.{MessageProcessor, MessageProducer}
import no_registrales.testkit.mocks.SujetoActorWithMockPersistence
import readside.proyectionists.no_registrales.objeto.ObjetoProjectionHandler
import readside.proyectionists.no_registrales.obligacion.ObligacionProjectionHandler
import readside.proyectionists.no_registrales.sujeto.SujetoProjectionHandler

trait NoRegistralesTestSuiteMock extends BaseE2ESpec {
  def testContext()(implicit system: ActorSystem): TestContext = new MockE2ETestContext()

  class MockE2ETestContext(implicit system: ActorSystem) extends BaseE2ETestContext {

    implicit val ec = system.dispatcher

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

    override val cassandraRead: CassandraRead = cassandraTestkit.cassandraRead
    override val cassandraWrite: CassandraWrite = cassandraTestkit.cassandraWrite

    val kafkaMock: KafkaMock = new KafkaMock()
    override def messageProducer: MessageProducer = kafkaMock
    override def messageProcessor: MessageProcessor with MessageProcessorLogging = kafkaMock

    val obligacionProyectionist: ObligacionProjectionHandler =
      new readside.proyectionists.no_registrales.obligacion.ObligacionProjectionHandler() {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }

    val objetoProyectionist: ObjetoProjectionHandler =
      new readside.proyectionists.no_registrales.objeto.ObjetoProjectionHandler() {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }

    val sujetoProyectionist: SujetoProjectionHandler =
      new readside.proyectionists.no_registrales.sujeto.SujetoProjectionHandler() {
        override val cassandra: CassandraWriteMock = cassandraTestkit.cassandraWrite
      }

    val sujeto: ActorRef =
      new SujetoActorWithMockPersistence(
        obligacionProyectionist,
        objetoProyectionist,
        sujetoProyectionist,
        _ => (),
        messageProducer
      ).start

    val cotitularidadActor: ActorRef = new CotitularidadActorWithMockPersistence(messageProducer).start

    val Query: NoRegistralesQueryTestKit with AgainstActors =
      new NoRegistralesQueryWithActorRef(sujeto)

    val MessageProducers = new MessageTestkitUtils(sujeto, cotitularidadActor, messageProducer)

    MessageProducers.StartMessageProcessor(messageProcessor).startProcessing()

    def close(): Unit = {}

  }
}
