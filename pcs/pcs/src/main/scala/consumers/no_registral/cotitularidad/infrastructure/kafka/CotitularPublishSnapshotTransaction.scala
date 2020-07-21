package consumers.no_registral.cotitularidad.infrastructure.kafka

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.cotitularidad.application.entities.CotitularidadCommands.CotitularidadPublishSnapshot
import consumers.no_registral.cotitularidad.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaClassicTypedAsk
import play.api.libs.json.Json

import scala.concurrent.Future

case class CotitularPublishSnapshotTransaction()(implicit actorRef: ActorRef) extends ActorTransaction {

  val topic = "CotitularidadPublishSnapshot"

  override def transaction(input: String): Future[Done] = {
    val cotitularidadPublishSnapshot: CotitularidadPublishSnapshot = Json.parse(input).as[CotitularidadPublishSnapshot]
    actorRef.ask[akka.Done](cotitularidadPublishSnapshot)

  }
}
