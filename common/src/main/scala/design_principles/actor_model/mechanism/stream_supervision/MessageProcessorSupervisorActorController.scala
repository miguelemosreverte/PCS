package design_principles.actor_model.mechanism.stream_supervision

import akka.actor.ActorSystem
import api.actor_transaction.ActorTransactionController

class StreamAdministrator(streamsControllers: Set[ActorTransactionController])(
    implicit actorSystem: ActorSystem
) {
  def start(): Unit = {
    MessageProcessorSupervisorActor.startAll(messageProcessorSupervisorActorRequirements, actorSystem)
  }
  def stop(): Unit = {
    MessageProcessorSupervisorActor.stopAll(messageProcessorSupervisorActorRequirements, actorSystem)
  }
}
