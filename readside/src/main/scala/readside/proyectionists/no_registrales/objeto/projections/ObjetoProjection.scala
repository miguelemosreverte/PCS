package readside.proyectionists.no_registrales.objeto.projections

import consumers.no_registral.objeto.domain.ObjetoEvents
import readside.proyectionists.common.shared.UpdateReadSideProjection

trait ObjetoProjection extends UpdateReadSideProjection[ObjetoEvents] {
  def collectionName: String = "read_side.buc_sujeto_objeto"
  val keys: List[(String, Object)] = List(
    "soj_suj_identificador" -> event.sujetoId,
    "soj_tipo_objeto" -> event.tipoObjeto,
    "soj_identificador" -> event.objetoId
  )
}
