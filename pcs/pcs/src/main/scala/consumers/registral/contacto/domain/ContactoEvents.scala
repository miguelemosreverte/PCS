package consumers.registral.contacto.domain

sealed trait ContactoEvents extends design_principles.actor_model.Event
object ContactoEvents {
  case class ContactoUpdatedFromDto(aggregateRoot: String, registro: ContactoExternalDto) extends ContactoEvents
}
