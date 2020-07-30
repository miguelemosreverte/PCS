package consumers.no_registral.cotitularidad.infrastructure.kafka

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.cotitularidad.application.entities.CotitularidadCommands.CotitularidadPublishSnapshot
import consumers.no_registral.cotitularidad.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaClassicTypedAsk
import monitoring.Monitoring
import play.api.libs.json.Json

case class CotitularPublishSnapshotTransaction(monitoring: Monitoring)(implicit actorRef: ActorRef,
                                                                       ec: ExecutionContext)
    extends ActorTransaction[CotitularidadPublishSnapshot](monitoring) {

  val topic = "CotitularidadPublishSnapshot"

  def processCommand(cmd: CotitularidadPublishSnapshot): Future[Done] = {
    actorRef.ask[akka.Done](cmd)
  }
}
