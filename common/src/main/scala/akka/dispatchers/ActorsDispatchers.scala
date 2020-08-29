package akka.dispatchers

import com.typesafe.config.Config
import cqrs.base_actor.typed.BasePersistentShardedTypedActorWithCQRS
import cqrs.base_actor.untyped.PersistentBaseActor

class ActorsDispatchers(config: Config) {

  private val typedActors = utils.Inference.getSubtypesNames[BasePersistentShardedTypedActorWithCQRS[_, _, _]]
  private val untypedActors = utils.Inference.getSubtypesNames[PersistentBaseActor[_, _]]

  private val actors = typedActors ++ untypedActors

  private val strongScalingDispatcher: StrongScaling =
    StrongScaling.apply(config)

  val actorsDispatchers = actors
    .map {
      strongScalingDispatcher.strongScalingDispatcher
    }
    .mkString("\n" * 3)
}
