import java.util.UUID

import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Terminated}
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.typed.{Cluster, Subscribe}
import akka.kafka.ConsumerRebalanceEvent
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import kafka.{KafkaMessageProcessorRequirements, TopicListener}
import monitoring.{KamonMonitoring, Monitoring}
import org.slf4j.LoggerFactory
import serialization.EventSerializer

object Main extends App {
  lazy val config = Seq(
    ConfigFactory.load(),
    ConfigFactory parseString EventSerializer.eventAdapterConf,
    ConfigFactory parseString EventSerializer.serializationConf
  ).reduce(_ withFallback _)

  startNode(
    config.getString("akka.remote.artery.canonical.port").toInt,
    config.getString("akka.management.http.port").toInt,
    config.getInt("http.port")
  )

  def startNode(remotingPort: Int, akkaManagementPort: Int, frontEndPort: Int): Unit = {
    ActorSystem(
      Behaviors.setup[MemberUp] { ctx =>
        val a: ActorContext[MemberUp] = ctx
        implicit val s = ctx.system
        implicit val sc = s.toClassic
        implicit val mat = Materializer.createMaterializer(ctx.system.toClassic)
        implicit val ec = ctx.executionContext
        AkkaManagement(ctx.system.toClassic).start()
        // maybe don't start until part of the cluster, or add health check
        val cluster = Cluster(ctx.system)
        cluster.subscriptions.tell(Subscribe(ctx.self, classOf[MemberUp]))
        Behaviors
          .receiveMessage[MemberUp] {
            case MemberUp(member) if member.uniqueAddress == cluster.selfMember.uniqueAddress =>
              ctx.log.info("Joined the cluster. Starting sharding and kafka processor")

              import akka.actor.typed.scaladsl.adapter._
              val rebalancerListener: ActorRef[ConsumerRebalanceEvent] =
                ctx.spawn(TopicListener(this.getClass.getName + UUID.randomUUID()), "rebalancerRef" + UUID.randomUUID())

              val monitoring: Monitoring = new KamonMonitoring
              implicit val transactionRequirements: KafkaMessageProcessorRequirements =
                KafkaMessageProcessorRequirements.productionSettings(Some(rebalancerListener.toClassic), monitoring)
              val sujetoActor = SujetoActor.startWithRequirements(monitoring)
              /*  SujetoNoTributarioTransaction(sujetoActor, monitoring).start(transactionRequirements)
              SujetoTributarioTransaction(sujetoActor, monitoring).start(transactionRequirements)
              ObjetoNoTributarioTransaction(sujetoActor, monitoring).start(transactionRequirements)
              ObjetoTributarioTransaction(sujetoActor, monitoring).start(transactionRequirements)
              ObligacionTributariaTransaction(sujetoActor, monitoring).start(transactionRequirements)
              ObligacionNoTributariaTransaction(sujetoActor, monitoring).start(transactionRequirements)
              ObjetoExencionTransaction(sujetoActor, monitoring).start(transactionRequirements)
              ObjetoUpdateCotitularesTransaction(sujetoActor, monitoring).start(transactionRequirements)
              ObjetoUpdateNovedadTransaction(sujetoActor, monitoring).start(transactionRequirements)

               */

              val log = LoggerFactory.getLogger(this.getClass)

              lazy val config = Seq(
                ConfigFactory.load(),
                ConfigFactory parseString EventSerializer.eventAdapterConf,
                ConfigFactory parseString EventSerializer.serializationConf
              ).reduce(_ withFallback _)

              AkkaManagement(s).start()
              ClusterBootstrap(s).start()

              ctx.spawn(Routes.apply(monitoring), "routes")

              Behaviors.same
            case MemberUp(member) =>
              ctx.log.info("Member up {}", member)
              Behaviors.same
          }
          .receiveSignal {
            case (ctx, Terminated(_)) =>
              ctx.log.warn("Kafka event processor stopped. Shutting down")
              Behaviors.stopped
          }
      },
      "PersonClassificationService",
      config
    )

  }

}
