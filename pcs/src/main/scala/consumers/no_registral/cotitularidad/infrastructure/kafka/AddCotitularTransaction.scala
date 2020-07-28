package consumers.no_registral.cotitularidad.infrastructure.kafka

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.cotitularidad.application.entities.CotitularidadCommands.CotitularidadAddSujetoCotitular
import consumers.no_registral.cotitularidad.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaClassicTypedAsk
import monitoring.Monitoring
import play.api.libs.json.Json

case class AddCotitularTransaction()(implicit actorRef: ActorRef, ec: ExecutionContext, monitoring: Monitoring)
    extends ActorTransaction[CotitularidadAddSujetoCotitular](monitoring) {

  val topic = "AddCotitularTransaction"

  override def processCommand(command: CotitularidadAddSujetoCotitular): Future[Done] =
    actorRef ask command

}
