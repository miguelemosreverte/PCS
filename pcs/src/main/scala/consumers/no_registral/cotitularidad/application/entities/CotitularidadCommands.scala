package consumers.no_registral.cotitularidad.application.entities

sealed trait CotitularidadCommands extends design_principles.actor_model.Command {
  def objetoId: String
  def tipoObjeto: String
  def aggregateRoot = s"Objeto-$objetoId-$tipoObjeto"
}

object CotitularidadCommands {

  case class CotitularidadAddSujetoCotitular(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      isResponsable: Option[Boolean],
      sujetoResponsable: Option[String]
  ) extends CotitularidadCommands

  case class CotitularidadPublishSnapshot(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      saldo: BigDecimal,
      vencimiento: Boolean,
      tags: Set[String],
      obligacionesSaldo: Map[String, BigDecimal] = Map.empty,
      obligacionesVencidasSaldo: Map[String, BigDecimal] = Map.empty
  ) extends CotitularidadCommands
}
