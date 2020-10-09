package spec.consumers.no_registrales.objeto.acceptance

import cassandra.CqlSessionSingleton
import cassandra.read.CassandraReadProduction
import cassandra.write.CassandraWriteProduction
import com.datastax.oss.driver.api.core.CqlSession
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import spec.consumers.no_registrales.objeto.ObjetoProjectionSpec

object ObjetoProjectionAcceptanceTest {

  val testContext: ObjetoProjectionSpec.TestContext = {
    implicit val session: CqlSession = CqlSessionSingleton.session
    ObjetoProjectionSpec.TestContext(
      write = new CassandraWriteProduction,
      read = new CassandraReadProduction {
        override def getRow(id: String): Option[Map[String, String]] = {
          val keys = ObjetoMessageRoots.extractor(id)
          super.getRow(
            s"select * from read_side.buc_sujeto_objeto" +
            s" where " +
            s"soj_suj_identificador='${keys.sujetoId}' " +
            s"and soj_identificador='${keys.objetoId}' " +
            s"and soj_tipo_objeto='${keys.tipoObjeto}' " +
            s"ALLOW FILTERING;"
          )
        }
      }
    )
  }

}

class ObjetoProjectionAcceptanceTest
    extends ObjetoProjectionSpec(
      _ => ObjetoProjectionAcceptanceTest.testContext
    )
