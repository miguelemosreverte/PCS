package design_principles.actor_model

trait Event extends ShardedMessage {
  override type ReturnType = Event.None
}

object Event {
  type None
}
