package consumers.no_registral.cotitularidad.infrastructure.kafka

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import consumers.no_registral.cotitularidad.application.entities.CotitularidadCommands.CotitularidadAddSujetoCotitular
import consumers.no_registral.cotitularidad.infrastructure.json.CotitularidadAddSujetoCotitularF
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.TypedAsk.AkkaClassicTypedAsk
import monitoring.Monitoring
import play.api.libs.json.Json
import serialization.maybeDecode

case class AddCotitularTransaction(actorRef: ActorRef, monitoring: Monitoring)(
    implicit
    actorTransactionRequirements: ActorTransactionRequirements
) extends ActorTransaction[CotitularidadAddSujetoCotitular](monitoring) {

  def topic = "AddCotitularTransaction"

  def processInput(input: String): Either[Throwable, CotitularidadAddSujetoCotitular] =
    maybeDecode[CotitularidadAddSujetoCotitular](input)

  def processCommand(cmd: CotitularidadAddSujetoCotitular): Future[Response.SuccessProcessing] = {
    actorRef.ask[Response.SuccessProcessing](cmd)
  }
}
