package akka.entity

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}

trait ShardedEntity[Requirements] extends ClusterEntity[Requirements] {

  import ShardedEntity._

  def props(requirements: Requirements): Props

  def startWithRequirements(requirements: Requirements)(
      implicit
      system: ActorSystem
  ): ActorRef = ClusterSharding(system).start(
    typeName = typeName,
    entityProps = props(requirements),
    settings = ClusterShardingSettings(system),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId(3)
  )
}

object ShardedEntity {
  trait ShardedEntityNoRequirements extends ShardedEntity[ShardedEntity.NoRequirements] {

    def start(
        implicit
        system: ActorSystem
    ): ActorRef = this.startWithRequirements(NoRequirements())
  }

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case s: Sharded => (s.entityId, s)
  }

  def extractShardId(numberOfShards: Int): ShardRegion.ExtractShardId = {
    case s: Sharded =>
      val result = EntityIdOps.fromEncodedToBigInt(EntityIdOps.encode(s.shardedId))
      val sharded = (result % numberOfShards).toString
      sharded

  }

  case class NoRequirements()

  trait Sharded {
    def entityId: String
    def shardedId: String
    def tupled: (String, String) = (entityId, shardedId)
  }
}
