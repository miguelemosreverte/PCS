package registrales.exencion.testkit.query

import akka.actor.{ActorRef, ActorSystem}
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.application.entities.ObjetoQueries.GetStateExencion
import consumers.no_registral.objeto.application.entities.ObjetoResponses.{GetExencionResponse, GetObjetoResponse}
import design_principles.actor_model.testkit.QueryTestkit
import design_principles.actor_model.testkit.QueryTestkit.AgainstHTTP
import play.api.libs.json.Format
import consumers.no_registral.objeto.infrastructure.json._
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.application.entities.ObligacionResponses.GetObligacionResponse
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.application.entity.SujetoResponses.GetSujetoResponse
import consumers_spec.no_registrales.testkit.query.{NoRegistralesQueryWithActorRef, NoRegistralesQueryWithHTTP}
import spec.consumers.registrales.exencion.ExencionProyectionistSpec.ExencionMessageRoot

import scala.reflect.ClassTag

class ExencionQueryTestkitAgainstHTTP(implicit system: ActorSystem) extends ExencionQueryTestkit with AgainstHTTP {

  val NoRegistralesQueryTestKit = new NoRegistralesQueryWithHTTP()
  def getStateObligacion(obligacionExample: ObligacionMessageRoots): GetObligacionResponse =
    NoRegistralesQueryTestKit.getStateObligacion(obligacionExample)

  def getStateObjeto(objetoExample: ObjetoMessageRoots): GetObjetoResponse =
    NoRegistralesQueryTestKit.getStateObjeto(objetoExample)

  def getStateSujeto(sujetoExample: SujetoMessageRoots): GetSujetoResponse =
    NoRegistralesQueryTestKit.getStateSujeto(sujetoExample)

  def getStateExencion(getStateExencion: ExencionMessageRoot): GetExencionResponse = {
    val sujetoId = getStateExencion.sujetoId
    val objetoId = getStateExencion.objetoId
    val tipoObjeto = getStateExencion.tipoObjeto
    val exencionId = getStateExencion.exencionId
    ask[String, GetExencionResponse](
      s"0.0.0.0:8081/state/sujeto/$sujetoId/objeto/$objetoId/tipo/$tipoObjeto/obligacion/$exencionId"
    )
  }
}
