package consumers.no_registral.objeto.infrastructure.consumer

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosAnt
import consumers.no_registral.objeto.infrastructure.json._
import serialization.decodeF

import scala.concurrent.Future

case class ObjetoNoTributarioTransaction()(implicit actorRef: ActorRef) extends ActorTransaction {

  val topic = "DGR-COP-OBJETOS-ANT"

  override def transaction(input: String): Future[Done] = {

    val registro = decodeF[ObjetosAnt](input)

    val command: ObjetoCommands =
      if (registro.SOJ_ESTADO.contains("BAJA"))
        ObjetoCommands.SetBajaObjeto(
          sujetoId = registro.SOJ_SUJ_IDENTIFICADOR,
          objetoId = registro.SOJ_IDENTIFICADOR,
          tipoObjeto = registro.SOJ_TIPO_OBJETO,
          deliveryId = registro.EV_ID,
          registro = registro,
          isResponsable = None,
          sujetoResponsable = None
        )
      else
        ObjetoCommands.ObjetoUpdateFromAnt(
          sujetoId = registro.SOJ_SUJ_IDENTIFICADOR,
          objetoId = registro.SOJ_IDENTIFICADOR,
          tipoObjeto = registro.SOJ_TIPO_OBJETO,
          deliveryId = registro.EV_ID,
          registro = registro
        )

    actorRef.ask[akka.Done](command)

  }
}
