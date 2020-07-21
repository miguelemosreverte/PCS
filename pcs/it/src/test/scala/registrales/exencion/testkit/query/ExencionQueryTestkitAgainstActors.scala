package registrales.exencion.testkit.query

import akka.actor.ActorRef
import consumers.no_registral.objeto.application.entities.ObjetoMessage.{ExencionMessageRoot, ObjetoMessageRoots}
import consumers.no_registral.objeto.application.entities.ObjetoResponses.{GetExencionResponse, GetObjetoResponse}
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.application.entities.ObligacionResponses.GetObligacionResponse
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.application.entity.SujetoResponses.GetSujetoResponse
import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryWithActorRef
import design_principles.actor_model.testkit.QueryTestkit.AgainstActors

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
