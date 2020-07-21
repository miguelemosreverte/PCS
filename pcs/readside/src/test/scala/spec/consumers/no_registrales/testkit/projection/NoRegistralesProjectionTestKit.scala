package spec.consumers.no_registrales.testkit.projection

import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import design_principles.actor_model.testkit.ProjectionTestkit

trait NoRegistralesProjectionTestKit extends ProjectionTestkit {

  def getReadsideObligacion(obligacion: ObligacionMessageRoots): Map[String, String]
  def getReadsideObjeto(objeto: ObjetoMessageRoots): Map[String, String]
  def getReadsideSujeto(sujeto: SujetoMessageRoots): Map[String, String]

}
