package readside.proyectionists.registrales.domicilio_sujeto.projections

import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents
import cassandra.ReadSideProjection
trait DomicilioSujetoProjection extends ReadSideProjection[DomicilioSujetoEvents] {
  def collectionName: String = "read_side.buc_domicilios_sujeto"

  val keys: List[(String, Object)] = List(
    "bds_suj_identificador" -> event.sujetoId,
    "bds_dom_id" -> event.domicilioId
  )
}
