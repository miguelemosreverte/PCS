package consumers_spec.no_registrales.testsuite

import akka.actor.typed.scaladsl.adapter._
import akka.actor.{typed, ActorRef, ActorSystem}
import consumers.no_registral.cotitularidad.infrastructure.dependency_injection.CotitularidadActor
import consumers.no_registral.objeto.infrastructure.event_processor.Guardian
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers_spec.no_registrales.testkit.query.{NoRegistralesQueryTestKit, NoRegistralesQueryWithActorRef}
import design_principles.actor_model.testkit.KafkaTestkit
import design_principles.actor_model.testkit.QueryTestkit.AgainstActors
import design_principles.external_pub_sub.kafka.MessageProcessorLogging
import design_principles.projection.infrastructure.CassandraTestkitProduction
import kafka.{KafkaMessageProcessorRequirements, MessageProcessor, MessageProducer}
import monitoring.{DummyMonitoring, Monitoring}

trait NoRegistralesTestSuiteAcceptance extends NoRegistralesTestSuite {
  def testContext()(implicit system: ActorSystem): TestContext = new AcceptanceTestContext()

  class AcceptanceTestContext(implicit system: ActorSystem) extends TestContext {
    import system.dispatcher

    val monitoring: Monitoring = new DummyMonitoring
    lazy val sujeto: ActorRef = SujetoActor.startWithRequirements(monitoring)
    lazy val cotitularidadActor: ActorRef =
      CotitularidadActor.startWithRequirements(
        KafkaMessageProcessorRequirements.productionSettings(None, monitoring, system)
      )

    lazy val cassandraTestkit: CassandraTestkitProduction = CassandraTestkitProduction()
    lazy val kafka = new KafkaTestkit()

    // start feedback loop
    lazy val feedbackLoop: typed.ActorRef[Nothing] =
      system.spawn[Nothing](Guardian.apply(), "ObjetoNovedadCotitularidad")

    override def messageProducer: MessageProducer = kafka.messageProducer
    override def messageProcessor: MessageProcessor with MessageProcessorLogging = kafka.messageProcessor

    override val Query: NoRegistralesQueryTestKit with AgainstActors =
      new NoRegistralesQueryWithActorRef(sujeto)

    def close(): Unit = {}
  }
}
