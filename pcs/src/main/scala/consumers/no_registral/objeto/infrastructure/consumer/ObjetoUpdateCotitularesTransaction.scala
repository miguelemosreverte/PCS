package consumers.no_registral.objeto.infrastructure.consumer

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateCotitulares
import consumers.no_registral.objeto.infrastructure.json._
import monitoring.Monitoring
import serialization.decodeF

case class ObjetoUpdateCotitularesTransaction(monitoring: Monitoring)(implicit actorRef: ActorRef, ec: ExecutionContext)
    extends ActorTransaction(monitoring) {

  val topic = "ObjetoUpdatedCotitulares"

  override def transaction(input: String): Future[Done] =
    for {
      cmd <- processInput(input)
      done <- processCommand(cmd)
    } yield done

  def processInput(input: String): Future[ObjetoUpdateCotitulares] = Future {
    decodeF[ObjetoUpdateCotitulares](input)
  }

  def processCommand(cmd: ObjetoUpdateCotitulares): Future[Done] = {
    actorRef.ask[akka.Done](cmd)
  }
}
