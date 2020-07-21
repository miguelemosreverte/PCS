package spec.consumers.no_registrales.testkit.projection
import akka.actor.ActorSystem
import cassandra.read.CassandraRead
import consumers.no_registral.objeto.application.entities.ObjetoMessage
import consumers.no_registral.obligacion.application.entities.ObligacionMessage
import consumers.no_registral.sujeto.application.entity.SujetoMessage
import design_principles.actor_model.testkit.ProjectionTestkit.AgainstCassandra
import org.scalatest.concurrent.ScalaFutures

class NoRegistralesProjectionWithCassandra(implicit cassandraRead: CassandraRead, system: ActorSystem)
    extends NoRegistralesProjectionTestKit
    with AgainstCassandra
    with ScalaFutures {

  override def getReadsideObligacion(e: ObligacionMessage.ObligacionMessageRoots): Map[String, String] = {

    val sujetoId = e.sujetoId
    val objetoId = e.objetoId
    val tipoObjeto = e.tipoObjeto
    val obligacionId = e.obligacionId
    val query = "select * from read_side.buc_obligaciones" +
      s" where bob_suj_identificador = '$sujetoId' " +
      s"and bob_soj_identificador = '$objetoId' " +
      s"and bob_soj_tipo_objeto = '$tipoObjeto' " +
      s"and bob_obn_id = '$obligacionId' " +
      "ALLOW FILTERING"

    cassandraRead.getRow(query).futureValue.get
  }

  override def getReadsideObjeto(e: ObjetoMessage.ObjetoMessageRoots): Map[String, String] = {

    val sujetoId = e.sujetoId
    val objetoId = e.objetoId
    val tipoObjeto = e.tipoObjeto

    val query = "select * from read_side.buc_sujeto_objeto" +
      s" where soj_suj_identificador = '$sujetoId' " +
      s"and soj_identificador = '$objetoId' " +
      s"and soj_tipo_objeto = '$tipoObjeto' " +
      "ALLOW FILTERING"

    cassandraRead.getRow(query).futureValue.get
  }

  override def getReadsideSujeto(e: SujetoMessage.SujetoMessageRoots): Map[String, String] = {

    val query = "select * from read_side.buc_sujeto" +
      s" where suj_identificador = '${e.sujetoId}' " +
      "ALLOW FILTERING"

    cassandraRead.getRow(query).futureValue.get
  }

}
