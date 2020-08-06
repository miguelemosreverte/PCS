package consumers.no_registral.objeto.infrastructure.consumer

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateCotitulares
import consumers.no_registral.objeto.infrastructure.json._
import design_principles.actor_model.Response
import monitoring.Monitoring
import serialization.decodeF

case class ObjetoUpdateCotitularesTransaction(actorRef: ActorRef, monitoring: Monitoring)(implicit ec: ExecutionContext)
    extends ActorTransaction[ObjetoUpdateCotitulares](monitoring) {

  val topic = "ObjetoUpdatedCotitulares"

  def processCommand(cmd: ObjetoUpdateCotitulares): Future[Response.SuccessProcessing] = {
    actorRef.ask[Response.SuccessProcessing](cmd)
  }
}
