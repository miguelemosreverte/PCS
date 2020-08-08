package akka.projections.cassandra

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.sharding.typed.scaladsl.ShardedDaemonProcess
import akka.cluster.sharding.typed.{ClusterShardingSettings, ShardedDaemonProcessSettings}
import akka.projection.ProjectionBehavior
import akka.projections.{ProjectionHandler, ProjectionSettings}

object CassandraProjectionist {

  case class CassandraProjectionistRequirements[T](
      projectionSettings: ProjectionSettings,
      system: ActorSystem[_],
      projectionHandler: ProjectionHandler[T]
  )
  def startProjection[T](requirements: CassandraProjectionistRequirements[T]): Unit = {
    val system = requirements.system
    val projectionSettings = requirements.projectionSettings
    val projectionHandler = requirements.projectionHandler
    val shardingSettings = ClusterShardingSettings(system)
    val shardedDaemonProcessSettings = ShardedDaemonProcessSettings(system).withShardingSettings(shardingSettings)

    ShardedDaemonProcess(system).init(
      projectionSettings.name,
      projectionSettings.parallelism,
      _ =>
        ProjectionBehavior(
          CassandraProjectionFactory.createProjectionFor(
            system,
            projectionSettings.projectionId,
            projectionHandler
          )
        ),
      shardedDaemonProcessSettings,
      Some(ProjectionBehavior.Stop)
    )
  }
}