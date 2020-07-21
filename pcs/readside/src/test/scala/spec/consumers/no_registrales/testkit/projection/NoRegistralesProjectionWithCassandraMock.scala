package spec.consumers.no_registrales.testkit.projection

import cassandra.read.CassandraRead
import consumers.no_registral.objeto.application.entities.ObjetoMessage
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import consumers.no_registral.obligacion.application.entities.ObligacionMessage
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionPersistedSnapshot
import consumers.no_registral.sujeto.application.entity.SujetoMessage
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoSnapshotPersisted
import design_principles.actor_model.testkit.ProjectionTestkit.AgainstCassandraMock
import org.scalatest.concurrent.ScalaFutures
import readside.proyectionists.no_registrales.objeto.projections.ObjetoSnapshotPersistedProjection
import readside.proyectionists.no_registrales.obligacion.projectionists.ObligacionSnapshotProjection
import readside.proyectionists.no_registrales.sujeto.projections.SujetoSnapshotPersistedProjection

class NoRegistralesProjectionWithCassandraMock(cassandraRead: CassandraRead)
    extends NoRegistralesProjectionTestKit
    with AgainstCassandraMock
    with ScalaFutures {

  import consumers.no_registral.objeto.infrastructure.json._
  import consumers.no_registral.obligacion.infrastructure.json._
  import consumers.no_registral.sujeto.infrastructure.json._

  override def getReadsideObligacion(e: ObligacionMessage.ObligacionMessageRoots): Map[String, String] = {
    val sujetoId = e.sujetoId
    val objetoId = e.objetoId
    val tipoObjeto = e.tipoObjeto
    val obligacionId = e.obligacionId
    val key = ObligacionMessage.ObligacionMessageRoots(sujetoId, objetoId, tipoObjeto, obligacionId).toString
    val value = cassandraRead.getRow(key).futureValue.get("event")
    val snapshot = serialization.decodeF[ObligacionPersistedSnapshot](value)
    ObligacionSnapshotProjection(snapshot).bindings.map(t => (t._1, t._2.toString)).toMap
  }

  override def getReadsideObjeto(e: ObjetoMessage.ObjetoMessageRoots): Map[String, String] = {
    val sujetoId = e.sujetoId
    val objetoId = e.objetoId
    val tipoObjeto = e.tipoObjeto
    val key = ObjetoMessage.ObjetoMessageRoots(sujetoId, objetoId, tipoObjeto).toString
    val value = cassandraRead.getRow(key).futureValue.get("event")
    val snapshot = serialization.decodeF[ObjetoSnapshotPersisted](value)
    ObjetoSnapshotPersistedProjection(snapshot).bindings.map(t => (t._1, t._2.toString)).toMap
  }

  override def getReadsideSujeto(e: SujetoMessage.SujetoMessageRoots): Map[String, String] = {
    val sujetoId = e.sujetoId
    val key = SujetoMessage.SujetoMessageRoots(sujetoId).toString
    val value = cassandraRead.getRow(key).futureValue.get("event")
    val snapshot = serialization.decodeF[SujetoSnapshotPersisted](value)
    SujetoSnapshotPersistedProjection(snapshot).bindings.map(t => (t._1, t._2.toString)).toMap
  }
}
