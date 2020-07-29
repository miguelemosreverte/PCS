package cqrs

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.sharding.typed.HashCodeNoEnvelopeMessageExtractor
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityRef, EntityTypeKey}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import cqrs.BasePersistentShardedTypedActor.AbstractState
import design_principles.actor_model.mechanism.AbstractOverReplyTo.MessageWithAutomaticReplyTo
import design_principles.actor_model.{Command, Query}
import cqrs.typed.command.SyncEffectCommandBus
import cqrs.typed.query.SyncEffectQueryBus
import org.slf4j.LoggerFactory
import scala.reflect.ClassTag

import cqrs.typed.event.SyncEffectEventBus

abstract class BasePersistentShardedTypedActor[ActorMessages <: design_principles.actor_model.ShardedMessage: ClassTag,
                                               ActorEvents,
                                               State <: AbstractState[ActorMessages, ActorEvents, State]](
    state: State
)(implicit system: ActorSystem[_]) {

  val sharding: ClusterSharding = ClusterSharding(system)
  val TypeKey: EntityTypeKey[ActorMessages] = EntityTypeKey[ActorMessages](
    utils.Inference.getSimpleName(this.getClass.getName)
  )
  // define a message extractor that knows how to retrieve the entityId from a message
  // we plan on deploying on a 3-node cluster, as a rule of thumb there should be 10 times as many
  // shards as there are nodes, hence the numberOfShards value of 30
  val messageExtractor: HashCodeNoEnvelopeMessageExtractor[ActorMessages] =
    new HashCodeNoEnvelopeMessageExtractor[ActorMessages](numberOfShards = 30) {
      override def entityId(message: ActorMessages): String = message.aggregateRoot
    }
  val shardActor: ActorRef[ActorMessages] = sharding.init(
    Entity(TypeKey) { context =>
      persistentEntity(context.entityId, context.shard)
    }.withMessageExtractor(messageExtractor)
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

object BasePersistentShardedTypedActor {

  trait AbstractState[Commands, Events, State <: AbstractState[Commands, Events, State]]

  object CQRS {

    trait AbstractStateWithCQRS[Commands <: design_principles.actor_model.ShardedMessage,
                                Events,
                                State <: AbstractStateWithCQRS[Commands, Events, State]]
        extends AbstractState[MessageWithAutomaticReplyTo[Commands, Commands#ReturnType], Events, State] {}

    abstract class BasePersistentShardedTypedActorWithCQRS[
        ActorMessages <: design_principles.actor_model.ShardedMessage: ClassTag,
        ActorEvents,
        State <: AbstractStateWithCQRS[ActorMessages, ActorEvents, State]
    ](s: State)(implicit system: ActorSystem[_])
        extends BasePersistentShardedTypedActor[MessageWithAutomaticReplyTo[ActorMessages, ActorMessages#ReturnType],
                                                ActorEvents,
                                                State](s) {

      val commandBus = new SyncEffectCommandBus[ActorEvents, State](LoggerFactory.getLogger(getClass))
      val queryBus = new SyncEffectQueryBus[ActorEvents, State](LoggerFactory.getLogger(getClass))
      val eventBus = new SyncEffectEventBus[ActorEvents, State]()

      def commandHandler(
          state: State,
          command: MessageWithAutomaticReplyTo[ActorMessages, ActorMessages#ReturnType]
      ): Effect[ActorEvents, State] = {
        command.payload match {
          case query: Query =>
            queryBus.ask(state, query)(command.replyTo.asInstanceOf[ActorRef[Query#ReturnType]])
          case cmd: Command =>
            commandBus.publish(cmd)(command.replyTo.asInstanceOf[ActorRef[akka.Done]])
        }
      }

      override def eventHandler(state: State, event: ActorEvents): State =
        eventBus.publish(state, event)
    }

  }

}