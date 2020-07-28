package consumers.no_registral.objeto.infrastructure.consumer

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateCotitulares
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.infrastructure.json._
import monitoring.Monitoring
import serialization.decodeF

case class ObjetoUpdateCotitularesTransaction()(implicit actorRef: ActorRef,
                                                ec: ExecutionContext,
                                                monitoring: Monitoring)
    extends ActorTransaction[ObjetoUpdateCotitulares](monitoring) {

  val topic = "ObjetoUpdatedCotitulares"

  override def processCommand(cmd: ObjetoUpdateCotitulares): Future[Done] = {
    actorRef.ask[akka.Done](cmd)
  }
}
