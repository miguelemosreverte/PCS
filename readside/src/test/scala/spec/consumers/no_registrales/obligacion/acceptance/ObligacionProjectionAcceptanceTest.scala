package spec.consumers.no_registrales.obligacion.acceptance

import cassandra.CqlSessionSingleton
import cassandra.read.CassandraReadProduction
import cassandra.write.CassandraWriteProduction
import com.datastax.oss.driver.api.core.CqlSession
import spec.consumers.no_registrales.obligacion.ObligacionProjectionSpec

object ObligacionProjectionAcceptanceTest {

  val testContext: ObligacionProjectionSpec.TestContext = {
    implicit val session: CqlSession = CqlSessionSingleton.session
    ObligacionProjectionSpec.TestContext(
      write = new CassandraWriteProduction,
      read = new CassandraReadProduction {
        override def getRow(id: String): Option[Map[String, String]] = {

          super.getRow(s"select * from read_side.buc_obligaciones;")
        }
      }
    )
  }

}

class ObligacionProjectionAcceptanceTest
    extends ObligacionProjectionSpec(
      _ => ObligacionProjectionAcceptanceTest.testContext
    )
