import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ActorSystem, Terminated}
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.typed.{Cluster, Subscribe}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import serialization.EventSerializer

object Main extends App {
  lazy val config = Seq(
    ConfigFactory.load(),
    ConfigFactory parseString EventSerializer.eventAdapterConf,
    ConfigFactory parseString EventSerializer.serializationConf
  ).reduce(_ withFallback _)

  startNode()

  def startNode(): Unit = {
    ActorSystem(
      Behaviors.setup[MemberUp] { ctx =>
        implicit val mat = Materializer.createMaterializer(ctx.system.toClassic)
        val cluster = Cluster(ctx.system)
        cluster.subscriptions.tell(Subscribe(ctx.self, classOf[MemberUp]))
        Behaviors
          .receiveMessage[MemberUp] {
            case MemberUp(member) if member.uniqueAddress == cluster.selfMember.uniqueAddress =>
              AkkaManagement(ctx.system).start()
              ClusterBootstrap(ctx.system).start()
              ctx.log.info("Joined the cluster. Starting sharding and kafka processor")

              ctx.spawn(Routes.apply(ctx), "routes")

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
