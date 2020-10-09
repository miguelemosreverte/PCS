package consumers_spec.no_registrales.objeto.acceptance

import akka.actor.ActorSystem
import akka.entity.ShardedEntity
import akka.entity.ShardedEntity.ShardedEntityRequirements
import consumers.no_registral.cotitularidad.infrastructure.dependency_injection.CotitularidadActor
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers_spec.no_registrales.objeto.ObjetoSpec
import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryWithActorRef
import consumers_spec.no_registrales.testkit.{MessageTestkitUtils, MonitoringAndMessageProducerMock}
import design_principles.external_pub_sub.kafka.{KafkaMock, KafkaProduction}
import org.scalatest.Ignore

object ObjetoSpecAcceptance {
  def getContext(system: ActorSystem): ObjetoSpec.TestContext = {
    implicit val actorRequirements: ShardedEntity.ProductionMonitoringAndMessageProducer =
      MonitoringAndMessageProducerMock.production(system)
    val messageBroker = new KafkaProduction
    val ObjetoSpecQuery = {
      val sujetoActor =
        SujetoActor
          .startWithRequirements(
            actorRequirements
          )(system)
      val cotitularidadActor =
        CotitularidadActor
          .startWithRequirements(
            actorRequirements
          )(system)
      new MessageTestkitUtils(sujetoActor, cotitularidadActor)
        .StartMessageProcessor(messageBroker)
        .startProcessing()
      new NoRegistralesQueryWithActorRef(
        sujetoActor,
        cotitularidadActor
      )
    }
    ObjetoSpec TestContext (
      messageProducer = messageBroker,
      messageProcessor = messageBroker,
      Query = ObjetoSpecQuery
    )
  }
}
@Ignore
class ObjetoSpecAcceptance
    extends ObjetoSpec(
      ObjetoSpecAcceptance.getContext
    )
