package consumers.no_registral.cotitularidad.infrastructure.kafka

import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import consumers.no_registral.cotitularidad.application.entities.CotitularidadCommands.CotitularidadAddSujetoCotitular
import consumers.no_registral.cotitularidad.infrastructure.json._
import design_principles.actor_model.mechanism.TypedAsk.AkkaClassicTypedAsk
import play.api.libs.json.Json

import scala.concurrent.Future

case class AddCotitularTransaction()(implicit actorRef: ActorRef) extends ActorTransaction {

  val topic = "AddCotitularTransaction"

  override def transaction(input: String): Future[Done] = {

    val addCotitular: CotitularidadAddSujetoCotitular = Json.parse(input).as[CotitularidadAddSujetoCotitular]
    actorRef.ask[akka.Done](addCotitular)

  }
}
