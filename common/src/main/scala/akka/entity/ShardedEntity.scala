package akka.entity

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import design_principles.actor_model.mechanism.local_processing.LocalizedProcessingMessageExtractor

import scala.concurrent.ExecutionContext

trait ShardedEntity[Requirements] extends ClusterEntity[Requirements] {

  import ShardedEntity._

  def props(requirements: Requirements): Props

  def startWithRequirements(requirements: Requirements)(
      implicit
      shardedEntityRequirements: ShardedEntityRequirements
  ): ActorRef = ClusterSharding(shardedEntityRequirements.system).start(
    typeName = typeName,
    entityProps = props(requirements),
    settings = ClusterShardingSettings(shardedEntityRequirements.system),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId(3)
  )
}

object ShardedEntity {

  case class ShardedEntityRequirements(
      system: ActorSystem,
      executionContext: ExecutionContext
  )

  trait ShardedEntityNoRequirements extends ShardedEntity[ShardedEntity.NoRequirements] {

    def start(
        implicit
        shardedEntityRequirements: ShardedEntityRequirements
    ): ActorRef = this.startWithRequirements(NoRequirements())
  }

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case s: Sharded => (s.entityId, s)
  }

  def extractShardId(numberOfShards: Int): ShardRegion.ExtractShardId = {
    case s: Sharded =>
      new LocalizedProcessingMessageExtractor(120).shardId(s.shardedId)
  }

  case class NoRequirements()

  trait Sharded {
    def entityId: String
    def shardedId: String
    def tupled: (String, String) = (entityId, shardedId)
  }
}
