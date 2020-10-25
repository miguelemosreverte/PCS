package consumers_spec.no_registrales.cotitularidad.acceptance

import akka.Done
import akka.actor.ActorSystem
import akka.entity.ShardedEntity.{MonitoringAndMessageProducer, ShardedEntityRequirements}
import akka.stream.UniqueKillSwitch
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import com.typesafe.config.ConfigFactory
import consumers.no_registral.cotitularidad.application.entities.CotitularidadCommands.CotitularidadPublishSnapshot
import consumers.no_registral.cotitularidad.infrastructure.dependency_injection.CotitularidadActor
import consumers.no_registral.cotitularidad.infrastructure.dependency_injection.CotitularidadActor
import consumers.no_registral.cotitularidad.infrastructure.kafka.ObjetoSnapshotPersistedHandler
import consumers.no_registral.objeto.infrastructure.consumer.{
  ObjetoTributarioTransaction,
  ObjetoUpdateNovedadTransaction
}
import consumers.no_registral.obligacion.infrastructure.consumer.ObligacionTributariaTransaction
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers_spec.no_registrales.cotitularidad.CotitularidadSpec
import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryWithActorRef
import consumers_spec.no_registrales.testkit.{MessageTestkitUtils, MonitoringAndMessageProducerMock}
import design_principles.external_pub_sub.kafka.{KafkaMock, MessageProcessorLogging}
import kafka.{
  KafkaMessageProcessorRequirements,
  KafkaMessageProducer,
  KafkaTransactionalMessageProcessor,
  MessageProducer
}

import scala.concurrent.Future

object CotitularidadSpecAcceptance {
  def getContext(system: ActorSystem): CotitularidadSpec.TestContext = {

    val actorRequiremensts =
      MonitoringAndMessageProducerMock.production(system)

    val messageProducer: MessageProducer = actorRequiremensts.messageProducer

    val messageProcessor: KafkaTransactionalMessageProcessor with MessageProcessorLogging with MessageProducer =
      new KafkaTransactionalMessageProcessor(
        KafkaMessageProcessorRequirements.productionSettings(
          actorRequiremensts.rebalanceListener,
          actorRequiremensts.monitoring,
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

    val CotitularidadSpecQuery = {
      val sujetoActor =
        SujetoActor
          .startWithRequirements(
            actorRequiremensts
          )(system)
      val cotitularidadActor =
        CotitularidadActor
          .startWithRequirements(
            actorRequiremensts
          )(system)
      new MessageTestkitUtils(sujetoActor, cotitularidadActor)
        .StartMessageProcessor(
          messageProcessor
        )
        .startProcessing {
          implicit val actorTransactionRequirements: ActorTransactionRequirements = ActorTransactionRequirements(
            executionContext = scala.concurrent.ExecutionContext.Implicits.global,
            config = ConfigFactory.empty
          )
          val monitoring = MonitoringAndMessageProducerMock.dummy.monitoring
          Set(
            ObjetoSnapshotPersistedHandler(cotitularidadActor, monitoring),
            ObjetoTributarioTransaction(sujetoActor, monitoring),
            ObligacionTributariaTransaction(sujetoActor, monitoring),
            ObjetoUpdateNovedadTransaction(sujetoActor, monitoring)
          )
        }
      new NoRegistralesQueryWithActorRef(
        sujetoActor,
        cotitularidadActor
      )
    }
    CotitularidadSpec TestContext (
      messageProducer = messageProducer,
      messageProcessor = messageProcessor,
      Query = CotitularidadSpecQuery
    )
  }
}

class CotitularidadSpecAcceptance
    extends CotitularidadSpec(
      CotitularidadSpecAcceptance.getContext
    )
