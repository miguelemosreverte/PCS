package design_principles.actor_model.context_provider

import com.typesafe.config.Config

case class ActorSystemRequirements(
    actorSystemName: String,
    config: Config
)
