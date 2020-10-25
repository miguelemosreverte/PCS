package spec.consumers.no_registrales.obligacion

import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import cassandra.write.CassandraWrite
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.domain
import consumers.no_registral.obligacion.domain.ObligacionEvents
import design_principles.actor_model.ActorSpec
import infrastructure.cassandra.CassandraTestkit.{TableName, _}
import readside.proyectionists.no_registrales.obligacion.projectionists.ObligacionSnapshotProjection
import stubs.consumers.no_registrales.obligacion.ObligacionEvents.{obligacionPersistedSnapshot, obligacionRemovedStub}

object ObligacionProjectionSpec {
  case class TestContext(
      write: CassandraWrite,
      read: CassandraRead
  )
}
abstract class ObligacionProjectionSpec(
    testContext: ActorSystem => ObligacionProjectionSpec.TestContext
) extends ActorSpec {
  implicit val tableName: TableName = TableName("read_side.buc_obligaciones")

  "ObligacionUpdated" should
  s"updated the row from readside.buc_obligaciones" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)

    val sujetoId = "Sujeto" + "1" + utils.Inference.getSimpleName(this.getClass.getName)
    val objetoId = "Objeto1" + utils.Inference.getSimpleName(this.getClass.getName)
    val tipoObjeto = "I"
    val obligacionId = "Obligacion1" + utils.Inference.getSimpleName(this.getClass.getName)
    val snapshot = obligacionPersistedSnapshot.copy(
      sujetoId = sujetoId,
      objetoId = objetoId,
      tipoObjeto = tipoObjeto,
      obligacionId = obligacionId
    )

    context.write
      .writeState(
        ObligacionSnapshotProjection(snapshot)
      )(scala.concurrent.ExecutionContext.global)
      .futureValue

    val mappedEvent: Map[String, String] =
      context.read
        .getRow(
          ObligacionMessageRoots(
            snapshot.sujetoId,
            snapshot.objetoId,
            snapshot.tipoObjeto,
            snapshot.obligacionId
          ).toString
        )
        .get

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
  }

  /*
  "ObligacionRemoved" should
  s"removed the row from readside.buc_obligaciones" in parallelActorSystemRunner { implicit s =>
    val context = testContext(s)
val sujetoId = "Sujeto" + "2" + utils.Inference.getSimpleName(this.getClass.getName)
    val objetoId = "Objeto1" + utils.Inference.getSimpleName(this.getClass.getName)
    val tipoObjeto = "I"
    val obligacionId = "Obligacion1" + utils.Inference.getSimpleName(this.getClass.getName)
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

    projectionTestkit processEnvelope snapshot
    validateObligacionReadside(context)(snapshot)
    projectionTestkit processEnvelope remove
    (projectionTestkit read rowId) ========================= Map.empty
  }*/
}
