package spec.consumers.registrales.exencion.acceptance

import cassandra.CqlSessionSingleton
import cassandra.read.CassandraReadProduction
import cassandra.write.CassandraWriteProduction
import com.datastax.oss.driver.api.core.CqlSession
import spec.consumers.registrales.exencion.ExencionProjectionSpec

object ExencionProjectionAcceptanceTest {

  val testContext: ExencionProjectionSpec.TestContext = {
    implicit val session: CqlSession = CqlSessionSingleton.session
    ExencionProjectionSpec.TestContext(
      write = new CassandraWriteProduction,
      read = new CassandraReadProduction {
        override def getRow(id: String): Option[Map[String, String]] = {
          super.getRow(s"select * from read_side.buc_exenciones;")
        }
      }
    )
  }

}

class ExencionProjectionAcceptanceTest
    extends ExencionProjectionSpec(
      _ => ExencionProjectionAcceptanceTest.testContext
    )
