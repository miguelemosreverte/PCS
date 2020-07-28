package consumers.no_registral.objeto.infrastructure.consumer

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosAnt
import consumers.no_registral.objeto.infrastructure.json._
import monitoring.Monitoring
import serialization.decodeF

case class ObjetoNoTributarioTransaction(monitoring: Monitoring)(implicit actorRef: ActorRef, ec: ExecutionContext)
    extends ActorTransaction(monitoring) {

  val topic = "DGR-COP-OBJETOS-ANT"

  override def transaction(input: String): Future[Done] =
    for {
      cmd <- processInput(input)
      done <- processCommand(cmd)
    } yield done

  def processInput(input: String): Future[ObjetosAnt] = Future {
    decodeF[ObjetosAnt](input)
  }

  def processCommand(registro: ObjetosAnt): Future[Done] = {
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
