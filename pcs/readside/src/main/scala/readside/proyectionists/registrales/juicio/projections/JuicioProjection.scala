package readside.proyectionists.registrales.juicio.projections

import cassandra.CassandraTypesAdapter.int
import consumers.registral.juicio.domain.JuicioEvents
import readside.proyectionists.common.shared.UpdateReadSideProjection

trait JuicioProjection extends UpdateReadSideProjection[JuicioEvents] {
  def collectionName: String = "read_side.buc_juicios"

  val keys: List[(String, Object)] = List(
    "bju_suj_identificador" -> event.sujetoId,
    "bju_soj_identificador" -> event.objetoId,
    "bju_soj_tipo_objeto" -> event.tipoObjeto,
    "bju_jui_id" -> int(event.juicioId.toInt)
  )
}
