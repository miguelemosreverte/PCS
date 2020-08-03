import akka.Done
import akka.pattern.retry
import akka.actor.ActorSystem
import akka.actor.Scheduler
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.kafka.ConsumerSettings
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.util.Timeout
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoCommands.ActividadSujetoUpdateFromDto
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoExternalDto.ActividadSujeto
import consumers.registral.actividad_sujeto.infrastructure.dependency_injection.ActividadSujetoActor
import kafka.TopicListener
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try

object UserEventsKafkaProcessor {

  sealed trait Command
  private case class KafkaConsumerStopped(reason: Try[Any]) extends Command

  def apply()(implicit system: akka.actor.typed.ActorSystem[_], mat: Materializer): Behavior[Nothing] = {
    Behaviors
      .setup[Command] { ctx =>
        val processorSettings = ProcessorConfig(ctx.system.settings.config.getConfig("kafka-to-sharding-processor"))
        implicit val classic: ActorSystem = ctx.system.toClassic
        implicit val ec: ExecutionContextExecutor = ctx.executionContext
        implicit val scheduler: Scheduler = classic.scheduler
        // TODO config
        val timeout = Timeout(3.seconds)
        val rebalancerRef = ctx
          .spawn(
            TopicListener(this.getClass.getName),
            "rebalancerRef"
          )
        val shardRegion = ActividadSujetoActor()
        val consumerSettings =
          ConsumerSettings(ctx.system.toClassic, new StringDeserializer, new StringDeserializer)
            .withBootstrapServers(processorSettings.bootstrapServers)
            .withGroupId(processorSettings.groupId)
            .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            .withStopTimeout(0.seconds)

        val subscription =
          Subscriptions
            .topics(processorSettings.topic)
            .withRebalanceListener(rebalancerRef.toClassic)

        val kafkaConsumer: Source[ConsumerRecord[String, String], Consumer.Control] =
          Consumer.plainSource(consumerSettings, subscription)
        ctx.log.info(s"starting stream for ${processorSettings.topic}")

        // TODO use committable source and reliable delivery (once released)?
        val stream: Future[Done] = kafkaConsumer
          .log("kafka-consumer")
          //.filter(_.key() != null) // no entity id
          .mapAsync(20) { record: ConsumerRecord[String, String] =>
            // alternatively the user id could be in the message rather than use the kafka key
            ctx.log.info(s"entityId->partition ${record.key()}->${record.partition()}")
            ctx.log.info("Forwarding message for entity {} to cluster sharding", record.key())
            // idempotency?

            import serialization.decodeF
            import consumers.registral.actividad_sujeto.infrastructure.json._
            val registro = decodeF[ActividadSujeto](record.value())
            val command =
              ActividadSujetoUpdateFromDto(
                sujetoId = registro.BAT_SUJ_IDENTIFICADOR,
                actividadSujetoId = registro.BAT_ATD_ID,
                deliveryId = BigInt(registro.EV_ID),
                registro = registro
              )
            import design_principles.actor_model.mechanism.TypedAsk._
            shardRegion ask command
          }
          .runWith(Sink.ignore)

        stream.onComplete { result =>
          ctx.self ! KafkaConsumerStopped(result)
        }
        Behaviors.receiveMessage[Command] {
          case KafkaConsumerStopped(reason) =>
            ctx.log.info("Consumer stopped {}", reason)
            Behaviors.stopped
        }
      }
      .narrow
  }

}
