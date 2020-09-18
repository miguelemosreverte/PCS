package akka.dispatchers

import akka.actor.Actor
import akka.persistence.PersistentActor
import com.typesafe.config.Config
import cqrs.base_actor.typed.BasePersistentShardedTypedActorWithCQRS
import cqrs.base_actor.untyped.PersistentBaseActor

class ActorsDispatchers(config: Config) {

  private val BasePersistentShardedTypedActorWithCQRSs =
    utils.Inference.getSubtypesNames[BasePersistentShardedTypedActorWithCQRS[_, _, _]]
  private val PersistentBaseActors = utils.Inference.getSubtypesNames[PersistentBaseActor[_, _]]
  private val PersistentActors = utils.Inference.getSubtypesNames[PersistentActor]
  private val Actors = utils.Inference.getSubtypesNames[Actor]

  private val actors: Seq[String] = Seq(
    BasePersistentShardedTypedActorWithCQRSs,
    PersistentBaseActors,
    PersistentActors,
    Actors
  ).flatten

  private val strongScalingDispatcher: StrongScaling =
    StrongScaling.apply(config)

  val actorsDispatchers = actors
    .map {
      strongScalingDispatcher.strongScalingDispatcher
    }
    .mkString("\n" * 3)
}
