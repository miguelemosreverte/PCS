package cqrs.base_actor.typed

import scala.reflect.ClassTag

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.sharding.external.ExternalShardAllocationStrategy
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityRef, EntityTypeKey}
import akka.cluster.sharding.typed.{ClusterShardingSettings, HashCodeNoEnvelopeMessageExtractor}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import design_principles.actor_model.mechanism.AbstractOverReplyTo.MessageWithAutomaticReplyTo
import design_principles.actor_model.mechanism.local_processing.LocalizedProcessingMessageExtractor

abstract class BasePersistentShardedTypedActor[
    ActorMessages <: design_principles.actor_model.ShardedMessage: ClassTag,
    ActorEvents,
    State <: BasePersistentShardedTypedActorAbstractState[ActorMessages, ActorEvents, State]
](
    state: State
)(implicit system: ActorSystem[_]) {

  val sharding: ClusterSharding = ClusterSharding(system)
  val TypeKey: EntityTypeKey[ActorMessages] = EntityTypeKey[ActorMessages](
    utils.Inference.getSimpleName(this.getClass.getName)
  )

  // define a message extractor that knows how to retrieve the entityId from a message
  // we plan on deploying on a 3-node cluster, as a rule of thumb there should be 10 times as many
  // shards as there are nodes, hence the numberOfShards value of 30
  // val messageExtractor: HashCodeNoEnvelopeMessageExtractor[ActorMessages] =
  //   new HashCodeNoEnvelopeMessageExtractor[ActorMessages](numberOfShards = 30) {
  //     override def entityId(message: ActorMessages): String = message.aggregateRoot.hashCode.toString
  //   }

  val shardActor: ActorRef[ActorMessages] = sharding.init(
    Entity(TypeKey) { context =>
      persistentEntity(context.entityId, context.shard)
    }.withAllocationStrategy(new ExternalShardAllocationStrategy(system, TypeKey.name))
      .withMessageExtractor(new LocalizedProcessingMessageExtractor[ActorMessages](120))
      .withSettings(ClusterShardingSettings(system))
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
      ).withTagger(tags)
    }

  def getEntityRef(entityId: String)(implicit system: ActorSystem[_]): EntityRef[ActorMessages] =
    sharding.entityRefFor(TypeKey, entityId)

  def getEntityRefTyped[InMessage <: design_principles.actor_model.ShardedMessage, Response](
      entityId: String
  )(implicit system: ActorSystem[_]): EntityRef[MessageWithAutomaticReplyTo[InMessage, Response]] = {
    val TypeKey = EntityTypeKey[MessageWithAutomaticReplyTo[InMessage, Response]](
      utils.Inference.getSimpleName(this.getClass.getName)
    )
    sharding.entityRefFor(TypeKey, entityId)
  }
}

object BasePersistentShardedTypedActor {}
