package consumers.no_registral.objeto.infrastructure.consumer

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoSnapshot
import consumers.no_registral.objeto.infrastructure.json._
import serialization.decodeF

import scala.concurrent.Future

case class ObjetoUpdateNovedadTransaction()(implicit actorRef: ActorRef) extends ActorTransaction {

  val topic = "ObjetoReceiveSnapshot"

  override def transaction(input: String): Future[Done] = {

    val cmd: ObjetoSnapshot = decodeF[ObjetoSnapshot](input)
    actorRef.ask[akka.Done](cmd)
  }
}
