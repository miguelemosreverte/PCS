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

case class AddCotitularTransaction(monitoring: Monitoring)(implicit actorRef: ActorRef, ec: ExecutionContext)
    extends ActorTransaction(monitoring) {

  val topic = "AddCotitularTransaction"

  override def transaction(input: String): Future[Done] =
    for {
      cmd <- processInput(input)
      done <- processCommand(cmd)
    } yield done

  def processInput(input: String): Future[CotitularidadAddSujetoCotitular] = Future {
    Json.parse(input).as[CotitularidadAddSujetoCotitular]
  }

  def processCommand(cmd: CotitularidadAddSujetoCotitular): Future[Done] = {
    actorRef.ask[akka.Done](cmd)
  }
}
