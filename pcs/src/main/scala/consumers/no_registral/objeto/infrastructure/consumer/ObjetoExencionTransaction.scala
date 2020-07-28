package consumers.no_registral.objeto.infrastructure.consumer

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.cotitularidad.application.entities.CotitularidadCommands.CotitularidadPublishSnapshot
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import consumers.no_registral.objeto.infrastructure.json._
import monitoring.Monitoring
import serialization.decodeF

case class ObjetoExencionTransaction()(implicit actorRef: ActorRef, ec: ExecutionContext, monitoring: Monitoring)
    extends ActorTransaction[Exencion](monitoring) {

  val topic = "DGR-COP-EXENCIONES"

  override def processCommand(exencion: Exencion): Future[Done] = {
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
