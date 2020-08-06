package design_principles.actor_model

trait Response

object Response {
  case class SuccessProcessing() extends Response
}
