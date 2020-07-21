package readside.proyectionists.no_registrales.objeto.projections

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted

case class ObjetoSnapshotPersistedProjection(
    event: ObjetoSnapshotPersisted
) extends ObjetoProjection {
  val registro: Option[ObjetoExternalDto] = event.registro
  val fromRegistro: Option[List[(String, Option[Object])]] = registro map { registro =>
    List(
      "soj_cat_soj_id" -> registro.SOJ_CAT_SOJ_ID,
      "soj_descripcion" -> registro.SOJ_DESCRIPCION,
      "soj_estado" -> registro.SOJ_ESTADO,
      "soj_fecha_fin" -> registro.SOJ_FECHA_FIN,
      "soj_fecha_inicio" -> registro.SOJ_FECHA_INICIO,
      "soj_id_externo" -> registro.SOJ_ID_EXTERNO,
      "soj_otros_atributos" -> registro.SOJ_OTROS_ATRIBUTOS,
      "soj_base_imponible" -> registro.SOJ_BASE_IMPONIBLE
    )
  }

  val others: List[(String, BigDecimal)] = List(
    "soj_saldo" -> event.saldo
    //"soj_vencida" -> event.vencimiento,
    //"soj_cotitular_suj_identificador" -> event.sujetoResponsable,
    //"soj_etiquetas" -> event.tags.mkString(","), //set(snapshot.tags),
    //"soj_cotitulares" -> event.cotitulares,
    //"soj_porcentaje_cotitular" -> event.porcentajeResponsabilidad
  )

  val bindings: List[(String, Serializable)] = fromRegistro match {
    case Some(optionalAttributes) => optionalAttributes ++ others
    case None => others
  }
}
