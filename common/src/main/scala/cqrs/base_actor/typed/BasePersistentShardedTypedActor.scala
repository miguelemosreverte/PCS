package cqrs.base_actor.typed

import scala.reflect.ClassTag
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, DispatcherSelector}
import akka.cluster.sharding.external.ExternalShardAllocationStrategy
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityRef, EntityTypeKey}
import akka.cluster.sharding.typed.{ClusterShardingSettings, HashCodeNoEnvelopeMessageExtractor}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import com.typesafe.config.{Config, ConfigFactory}
import design_principles.actor_model.mechanism.AbstractOverReplyTo.MessageWithAutomaticReplyTo
import design_principles.actor_model.mechanism.local_processing.LocalizedProcessingMessageExtractor

import scala.util.Try

abstract class BasePersistentShardedTypedActor[
    ActorMessages <: design_principles.actor_model.ShardedMessage: ClassTag,
    ActorEvents,
    State <: BasePersistentShardedTypedActorAbstractState[ActorMessages, ActorEvents, State]
](
    state: State,
    config: Config
)(implicit system: ActorSystem[_]) {

  val sharding: ClusterSharding = ClusterSharding(system)
  val TypeKey: EntityTypeKey[ActorMessages] = EntityTypeKey[ActorMessages](
    utils.Inference.getSimpleName(this.getClass.getName)
  )

  val shardActor: ActorRef[ActorMessages] = sharding.init(
    Entity(TypeKey) { context =>
      persistentEntity(context.entityId, context.shard)
    }.withAllocationStrategy(new ExternalShardAllocationStrategy(system, TypeKey.name))
      .withMessageExtractor(new LocalizedProcessingMessageExtractor[ActorMessages](30))
      .withSettings(ClusterShardingSettings(system))
      .withEntityProps(DispatcherSelector.fromConfig(utils.Inference.getSimpleName(this.getClass.getName)))
  )

  def commandHandler(state: State, command: ActorMessages): Effect[ActorEvents, State]
  def eventHandler(state: State, event: ActorEvents): State

  def tags(event: ActorEvents): Set[String] = Set.empty
  // maybe remove this to a Singleton?

  def persistentEntity(entityId: String, shardedId: ActorRef[ClusterSharding.ShardCommand]): Behavior[ActorMessages] =
    Behaviors.setup { _ =>
      EventSourcedBehavior[
        ActorMessages,
        ActorEvents,
        State
      ](
        PersistenceId(TypeKey.name, entityId),
        emptyState = state,
        commandHandler = (state, message) => commandHandler(state, message),
        eventHandler = (state, evt) => eventHandler(state, evt)
      ).withTagger({ event =>
        val tagsWithShardId = tags(event) map { tag =>
          val parallelism = Try {
            config
              .getString(
                s"projectionist.$tag.paralellism"
              )
              .toInt
          }.getOrElse(1)
          val shardId = PersistenceId(TypeKey.name, entityId).hashCode.abs % parallelism
          s"$tag-$shardId"
        }
        tagsWithShardId
      })
    }

  def getEntityRef(entityId: String): EntityRef[ActorMessages] =
    sharding.entityRefFor(TypeKey, entityId)

  def getEntityRefTyped[InMessage <: design_principles.actor_model.ShardedMessage, Response](
      entityId: String
  ): EntityRef[MessageWithAutomaticReplyTo[InMessage, Response]] = {
    val TypeKey = EntityTypeKey[MessageWithAutomaticReplyTo[InMessage, Response]](
      utils.Inference.getSimpleName(this.getClass.getName)
    )
    sharding.entityRefFor(TypeKey, entityId)
  }
}

object BasePersistentShardedTypedActor {}
