package design_principles.actor_model

trait Command extends ShardedMessage {
  override type ReturnType = akka.Done
  type EventType = Event.None
  def deliveryId: BigInt
}
