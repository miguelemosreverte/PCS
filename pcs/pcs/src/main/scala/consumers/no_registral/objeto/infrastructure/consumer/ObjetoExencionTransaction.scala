package consumers.no_registral.objeto.infrastructure.consumer

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import consumers.no_registral.objeto.infrastructure.json._
import serialization.decodeF

import scala.concurrent.Future

case class ObjetoExencionTransaction()(implicit actorRef: ActorRef) extends ActorTransaction {

  val topic = "DGR-COP-EXENCIONES"

  override def transaction(input: String): Future[Done] = {
    val exencion = decodeF[Exencion](input)
    val command = ObjetoCommands.ObjetoAddExencion(
      deliveryId = exencion.EV_ID,
      sujetoId = exencion.BEX_SUJ_IDENTIFICADOR,
      objetoId = exencion.BEX_SOJ_IDENTIFICADOR,
      tipoObjeto = exencion.BEX_SOJ_TIPO_OBJETO,
      exencion = exencion
    )

    actorRef.ask[akka.Done](command)
  }
}
