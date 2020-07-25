package akka.projections

import akka.actor.typed.ActorSystem
import com.typesafe.config.Config

object ProjectionSettings {

  def apply(system: ActorSystem[_]): ProjectionSettings = {
    apply(system.settings.config.getConfig("event-processor"))
  }

  def apply(config: Config): ProjectionSettings = {
    val tagPrefix: String = config.getString("tag")
    val parallelism: Int = config.getInt("parallelism")
    ProjectionSettings(tagPrefix, parallelism)
  }
}

final case class ProjectionSettings(tag: String, parallelism: Int)
