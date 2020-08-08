package spec.consumers.no_registrales.obligacion

import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.domain
import consumers.no_registral.obligacion.domain.ObligacionEvents
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import spec.testsuite.{ProjectionTestContext, ProjectionTestSuite}
import stubs.consumers.no_registrales.obligacion.ObligacionEvents.{obligacionPersistedSnapshot, obligacionRemovedStub}

trait ObligacionProjectionSpec extends ProjectionTestSuite[ObligacionEvents, ObligacionMessageRoots] {
  implicit val tableName: TableName = TableName("read_side.buc_obligaciones")

  def validateObligacionReadside(context: ProjectionTestContext[ObligacionEvents, ObligacionMessageRoots]): Any => Any = {
    case snapshot: domain.ObligacionEvents.ObligacionPersistedSnapshot =>
      val mappedEvent: Map[String, String] =
        context.ProjectionTestkit read ObligacionMessageRoots(
          snapshot.sujetoId,
          snapshot.objetoId,
          snapshot.tipoObjeto,
          snapshot.obligacionId
        )

      snapshot.registro match {
        case Some(registro) =>
          mappedEvent =========================
            Map(
              "bob_suj_identificador" -> snapshot.sujetoId,
              "bob_soj_tipo_objeto" -> snapshot.tipoObjeto,
              "bob_soj_identificador" -> snapshot.objetoId,
              "bob_obn_id" -> snapshot.obligacionId,
              "bob_capital" -> registro.BOB_CAPITAL,
              "bob_cuota" -> registro.BOB_CUOTA,
              "bob_estado" -> registro.BOB_ESTADO,
              "bob_concepto" -> registro.BOB_CONCEPTO,
              "bob_fiscalizada" -> registro.BOB_FISCALIZADA,
              "bob_impuesto" -> registro.BOB_IMPUESTO,
              "bob_interes_punit" -> registro.BOB_INTERES_PUNIT,
              "bob_interes_resar" -> registro.BOB_INTERES_RESAR,
              "bob_jui_id" -> registro.BOB_JUI_ID,
              "bob_otros_atributos" -> registro.BOB_OTROS_ATRIBUTOS,
              "bob_periodo" -> registro.BOB_PERIODO,
              "bob_pln_id" -> registro.BOB_PLN_ID,
              "bob_prorroga" -> registro.BOB_PRORROGA,
              "bob_tipo" -> registro.BOB_TIPO,
              "bob_total" -> registro.BOB_TOTAL,
              "bob_vencimiento" -> registro.BOB_VENCIMIENTO,
              "bob_saldo" -> snapshot.saldo
            )
        case None =>
          mappedEvent =========================
            Map(
              "bob_suj_identificador" -> snapshot.sujetoId,
              "bob_soj_tipo_objeto" -> snapshot.tipoObjeto,
              "bob_soj_identificador" -> snapshot.objetoId,
              "bob_obn_id" -> snapshot.obligacionId,
              "bob_saldo" -> snapshot.saldo
            )
      }
    case _ => ()

  }
  "ObligacionUpdated" should
  s"updated the row from readside.buc_obligaciones" in parallelActorSystemRunner { implicit s =>
    val context = testContext()
    val projectionTestkit = context.ProjectionTestkit

    val sujetoId = "Sujeto" + "1" + this.getClass.getName
    val objetoId = "Objeto1" + this.getClass.getName
    val tipoObjeto = "I"
    val obligacionId = "Obligacion1" + this.getClass.getName
    val snapshot = obligacionPersistedSnapshot.copy(
      sujetoId = sujetoId,
      objetoId = objetoId,
      tipoObjeto = tipoObjeto,
      obligacionId = obligacionId
    )
    projectionTestkit processEnvelope projectionTestkit.eventEnvelope(snapshot)
    val mappedEvent: Map[String, String] =
      projectionTestkit read ObligacionMessageRoots(
        snapshot.sujetoId,
        snapshot.objetoId,
        snapshot.tipoObjeto,
        snapshot.obligacionId
      )

    validateObligacionReadside(context)(snapshot)
    context.close()
  }

  "ObligacionRemoved" should
  s"removed the row from readside.buc_obligaciones" in parallelActorSystemRunner { implicit s =>
    val context = testContext()
    val projectionTestkit = context.ProjectionTestkit

    val sujetoId = "Sujeto" + "2" + this.getClass.getName
    val objetoId = "Objeto1" + this.getClass.getName
    val tipoObjeto = "I"
    val obligacionId = "Obligacion1" + this.getClass.getName
    val snapshot = obligacionPersistedSnapshot.copy(
      sujetoId = sujetoId,
      objetoId = objetoId,
      tipoObjeto = tipoObjeto,
      obligacionId = obligacionId
    )
    val registro = snapshot.registro.get
    val remove = obligacionRemovedStub.copy(
      sujetoId = sujetoId,
      objetoId = objetoId,
      tipoObjeto = tipoObjeto,
      obligacionId = obligacionId
    )

    val rowId = ObligacionMessageRoots(
      snapshot.sujetoId,
      snapshot.objetoId,
      snapshot.tipoObjeto,
      snapshot.obligacionId
    )

    projectionTestkit processEnvelope projectionTestkit.eventEnvelope(snapshot)
    validateObligacionReadside(context)(snapshot)
    projectionTestkit processEnvelope projectionTestkit.eventEnvelope(remove)
    (projectionTestkit read rowId) ========================= Map.empty
    context.close()
  }
}
