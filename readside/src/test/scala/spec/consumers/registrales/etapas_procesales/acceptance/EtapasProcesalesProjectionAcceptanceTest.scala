package spec.consumers.registrales.etapas_procesales.acceptance

import cassandra.CqlSessionSingleton
import cassandra.read.CassandraReadProduction
import cassandra.write.CassandraWriteProduction
import com.datastax.oss.driver.api.core.CqlSession
import spec.consumers.registrales.etapas_procesales.EtapasProcesalesProjectionSpec

object EtapasProcesalesProjectionAcceptanceTest {

  val testContext: EtapasProcesalesProjectionSpec.TestContext = {
    implicit val session: CqlSession = CqlSessionSingleton.session
    EtapasProcesalesProjectionSpec.TestContext(
      write = new CassandraWriteProduction,
      read = new CassandraReadProduction {
        override def getRow(id: String): Option[Map[String, String]] = {
          super.getRow(s"select * from read_side.buc_etapas_proc;")
        }
      }
    )
  }

}

class EtapasProcesalesProjectionAcceptanceTest
    extends EtapasProcesalesProjectionSpec(
      _ => EtapasProcesalesProjectionAcceptanceTest.testContext
    )
