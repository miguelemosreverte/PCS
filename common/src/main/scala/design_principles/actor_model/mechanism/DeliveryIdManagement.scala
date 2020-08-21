package design_principles.actor_model.mechanism

import design_principles.actor_model.{Command, Event}

object DeliveryIdManagement {
  def validateCommand(
      event: Event,
      command: Command,
      lastDeliveryIdByEvents: Map[String, BigInt]
  ): Boolean =
    command.deliveryId <= lastDeliveryIdByEvents.getOrElse(utils.Inference.getSimpleName(event.getClass.getName), BigInt(0))
}
