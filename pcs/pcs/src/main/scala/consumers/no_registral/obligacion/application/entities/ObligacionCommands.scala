package consumers.no_registral.obligacion.application.entities

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto.DetallesObligacion
import design_principles.actor_model.Command

sealed trait ObligacionCommands extends Command with ObligacionMessage

object ObligacionCommands {
  case class ObligacionUpdateFromDto(
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String,
      deliveryId: BigInt,
      registro: ObligacionExternalDto,
      detallesObligacion: Seq[DetallesObligacion]
  ) extends ObligacionCommands

  case class ObligacionRemove(
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String
  ) extends ObligacionCommands {
    override def deliveryId: BigInt = 0
  }

  case class ObligacionUpdateExencion(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String,
      exencion: Exencion
  ) extends ObligacionCommands
}
