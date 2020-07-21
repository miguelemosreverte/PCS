package registrales.exencion.testkit.query

import akka.actor.{ActorRef, ActorSystem}
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.application.entities.ObjetoQueries.{GetStateExencion, GetStateObjeto}
import consumers.no_registral.objeto.application.entities.ObjetoResponses.{GetExencionResponse, GetObjetoResponse}
import design_principles.actor_model.Query
import design_principles.actor_model.testkit.QueryTestkit.AgainstActors
import play.api.libs.json.Format
import consumers.no_registral.objeto.infrastructure.json._
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.application.entities.ObligacionQueries.GetStateObligacion
import consumers.no_registral.obligacion.application.entities.ObligacionResponses.GetObligacionResponse
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.application.entity.SujetoQueries.GetStateSujeto
import consumers.no_registral.sujeto.application.entity.SujetoResponses.GetSujetoResponse
import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryWithActorRef
import spec.consumers.registrales.exencion.ExencionProyectionistSpec
import spec.consumers.registrales.exencion.ExencionProyectionistSpec.ExencionMessageRoot

import scala.reflect.ClassTag

case class ExencionQueryTestkitAgainstActors(actor: ActorRef) extends ExencionQueryTestkit with AgainstActors {

  val NoRegistralesQueryTestKit = new NoRegistralesQueryWithActorRef(actor)
  def getStateObligacion(obligacionExample: ObligacionMessageRoots): GetObligacionResponse =
    NoRegistralesQueryTestKit.getStateObligacion(obligacionExample)

  def getStateObjeto(objetoExample: ObjetoMessageRoots): GetObjetoResponse =
    NoRegistralesQueryTestKit.getStateObjeto(objetoExample)

  def getStateSujeto(sujetoExample: SujetoMessageRoots): GetSujetoResponse =
    NoRegistralesQueryTestKit.getStateSujeto(sujetoExample)

  def getStateExencion(getStateExencion: ExencionMessageRoot): GetExencionResponse =
    actor.ask[GetExencionResponse](getStateExencion).futureValue

}
