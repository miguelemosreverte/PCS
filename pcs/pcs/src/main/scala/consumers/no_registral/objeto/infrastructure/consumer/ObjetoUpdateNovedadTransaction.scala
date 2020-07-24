package consumers.no_registral.objeto.infrastructure.consumer

import scala.concurrent.{ExecutionContext, Future}

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoSnapshot
import consumers.no_registral.objeto.infrastructure.json._
import serialization.decodeF

case class ObjetoUpdateNovedadTransaction()(implicit actorRef: ActorRef, ec: ExecutionContext)
    extends ActorTransaction {

  val topic = "ObjetoReceiveSnapshot"

  override def transaction(input: String): Future[Done] =
    for {
      cmd <- processInput(input)
      done <- processCommand(cmd)
    } yield done

  def processInput(input: String): Future[ObjetoSnapshot] = Future {
    decodeF[ObjetoSnapshot](input)
  }

  def processCommand(cmd: ObjetoSnapshot): Future[Done] = {
    actorRef.ask[akka.Done](cmd)
  }
}
