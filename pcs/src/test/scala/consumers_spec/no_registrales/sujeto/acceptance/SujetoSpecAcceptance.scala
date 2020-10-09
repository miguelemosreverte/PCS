package consumers_spec.no_registrales.sujeto.acceptance

import akka.Done
import akka.actor.ActorSystem
import akka.entity.ShardedEntity
import akka.entity.ShardedEntity.ShardedEntityRequirements
import akka.stream.UniqueKillSwitch
import consumers.no_registral.cotitularidad.infrastructure.dependency_injection.CotitularidadActor
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers_spec.no_registrales.sujeto.SujetoSpec
import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryWithActorRef
import consumers_spec.no_registrales.testkit.{MessageTestkitUtils, MonitoringAndMessageProducerMock}
import design_principles.external_pub_sub.kafka.{KafkaMock, KafkaProduction, MessageProcessorLogging}
import kafka.{
  KafkaMessageProcessorRequirements,
  KafkaMessageProducer,
  KafkaTransactionalMessageProcessor,
  MessageProducer
}

import scala.concurrent.Future

object SujetoSpecAcceptance {
  def getContext(system: ActorSystem): SujetoSpec.TestContext = {
    implicit val actorRequirements: ShardedEntity.ProductionMonitoringAndMessageProducer =
      MonitoringAndMessageProducerMock.production(system)

    val messageProducer: MessageProducer = actorRequirements.messageProducer

    val messageProcessor: KafkaTransactionalMessageProcessor with MessageProcessorLogging with MessageProducer =
      new KafkaTransactionalMessageProcessor(
        KafkaMessageProcessorRequirements.productionSettings(
          actorRequirements.rebalanceListener,
          actorRequirements.monitoring,
          system,
          system.dispatcher
        )
      ) with MessageProcessorLogging with MessageProducer {
        override def run(SOURCE_TOPIC: String,
                         SINK_TOPIC: String,
                         algorithm: String => Future[Seq[String]]): (Option[UniqueKillSwitch], Future[Done]) = {
          super.run(SOURCE_TOPIC, SINK_TOPIC, { string =>
            messageHistory = messageHistory :+ ((SOURCE_TOPIC, string))
            algorithm(string)
          })
        }

        override def createTopic(topic: String): Future[Done] =
          messageProducer.createTopic(topic)

        override def produce(data: Seq[KafkaMessageProducer.KafkaKeyValue],
                             topic: String)(handler: Seq[KafkaMessageProducer.KafkaKeyValue] => Unit): Future[Done] =
          messageProducer.produce(data, topic)(handler)

      }
    val SujetoSpecQueryAgainstActors = {
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
        .StartMessageProcessor(messageProcessor)
        .startProcessing()
      new NoRegistralesQueryWithActorRef(
        sujetoActor,
        cotitularidadActor
      )
    }
    SujetoSpec TestContext (
      messageProducer = messageProcessor,
      messageProcessor = messageProcessor,
      Query = SujetoSpecQueryAgainstActors
    )
  }
}
@Ignore
class SujetoSpecAcceptance
    extends SujetoSpec(
      SujetoSpecAcceptance getContext
    )
