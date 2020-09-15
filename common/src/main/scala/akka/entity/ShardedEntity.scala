package akka.entity

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import com.typesafe.config.Config
import design_principles.actor_model.mechanism.local_processing.LocalizedProcessingMessageExtractor
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

trait ShardedEntity[Requirements] extends ClusterEntity[Requirements] {

  import ShardedEntity._

  def props(requirements: Requirements): Props

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case s: Sharded => (s.entityId, s)
  }

  val numberOfShards = 3
  def extractShardId: ShardRegion.ExtractShardId = {
    case s: Sharded =>
      new LocalizedProcessingMessageExtractor(numberOfShards * 10).shardId(s.shardedId)
  }

  def clusterShardingSettings(
      implicit
      system: ActorSystem
  ) = ClusterShardingSettings(system)

  def startWithRequirements(requirements: Requirements)(
      implicit
      system: ActorSystem
  ): ActorRef = ClusterSharding(system).start(
    typeName = typeName,
    entityProps = props(requirements).withDispatcher(utils.Inference.getSimpleName(this.getClass.getName)),
    settings = clusterShardingSettings,
    extractEntityId = extractEntityId,
    extractShardId = extractShardId
  )
}

object ShardedEntity {

  case class MonitoringAndConfig(monitoring: Monitoring, config: Config)

  trait ShardedEntityNoRequirements extends ShardedEntity[ShardedEntity.NoRequirements] {

    def start(
        implicit
        system: ActorSystem
    ): ActorRef = this.startWithRequirements(NoRequirements())
  }

  case class NoRequirements()

  trait Sharded {
    def entityId: String
    def shardedId: String
    def tupled: (String, String) = (entityId, shardedId)
  }
}
