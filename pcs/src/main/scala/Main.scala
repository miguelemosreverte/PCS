import akka.actor.typed.ActorSystem
import akka.actor.typed.Terminated
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.typed.Cluster
import akka.cluster.typed.Subscribe
import akka.http.scaladsl._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.management.scaladsl.AkkaManagement
import akka.stream.Materializer
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import serialization.EventSerializer

import scala.concurrent.Future

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
        implicit val s = ctx.system
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
              val eventProcessor = ctx.spawn[Nothing](UserEventsKafkaProcessor(), "kafka-event-processor")
              ctx.watch(eventProcessor)
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
