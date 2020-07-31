package consumers.no_registral.objeto.infrastructure.consumer

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoSnapshot
import consumers.no_registral.objeto.infrastructure.json._
import monitoring.Monitoring
import serialization.decodeF

case class ObjetoUpdateNovedadTransaction(actorRef: ActorRef, monitoring: Monitoring)(implicit ec: ExecutionContext)
    extends ActorTransaction[ObjetoSnapshot](monitoring) {

  val topic = "ObjetoReceiveSnapshot"

  def processCommand(cmd: ObjetoSnapshot): Future[Done] = {
    actorRef.ask[akka.Done](cmd)
  }
}
