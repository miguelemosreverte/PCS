package akka.projections

import akka.actor.typed.ActorSystem
import com.typesafe.config.Config
import monitoring.Monitoring

object ProjectionSettings {

  def apply(system: ActorSystem[_], monitoring: Monitoring): ProjectionSettings = {
    apply(system.settings.config.getConfig("event-processor"), monitoring)
  }

  def apply(config: Config, monitoring: Monitoring): ProjectionSettings = {
    val tagPrefix: String = config.getString("tag")
    val parallelism: Int = config.getInt("parallelism")
    ProjectionSettings(tagPrefix, parallelism, monitoring)
  }
}

final case class ProjectionSettings(tag: String, parallelism: Int, monitoring: Monitoring)
