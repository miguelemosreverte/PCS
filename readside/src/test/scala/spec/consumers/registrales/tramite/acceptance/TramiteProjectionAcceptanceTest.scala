package spec.consumers.registrales.tramite.acceptance

import cassandra.CqlSessionSingleton
import cassandra.read.CassandraReadProduction
import cassandra.write.CassandraWriteProduction
import com.datastax.oss.driver.api.core.CqlSession
import spec.consumers.registrales.tramite.TramiteProjectionSpec

object TramiteProjectionAcceptanceTest {

  val testContext: TramiteProjectionSpec.TestContext = {
    implicit val session: CqlSession = CqlSessionSingleton.session
    TramiteProjectionSpec.TestContext(
      write = new CassandraWriteProduction,
      read = new CassandraReadProduction {
        override def getRow(id: String): Option[Map[String, String]] = {
          super.getRow(s"select * from read_side.buc_tramites;")
        }
      }
    )
  }

}

class TramiteProjectionAcceptanceTest
    extends TramiteProjectionSpec(
      _ => TramiteProjectionAcceptanceTest.testContext
    )
