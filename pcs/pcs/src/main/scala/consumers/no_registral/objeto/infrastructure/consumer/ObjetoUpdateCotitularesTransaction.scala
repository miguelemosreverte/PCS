package consumers.no_registral.objeto.infrastructure.consumer

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateCotitulares
import consumers.no_registral.objeto.infrastructure.json._
import serialization.decodeF

import scala.concurrent.Future

case class ObjetoUpdateCotitularesTransaction()(implicit actorRef: ActorRef) extends ActorTransaction {

  val topic = "ObjetoUpdatedCotitulares"

  override def transaction(input: String): Future[Done] = {

    val cmd: ObjetoUpdateCotitulares = decodeF[ObjetoUpdateCotitulares](input)
    actorRef.ask[akka.Done](cmd)
  }
}
