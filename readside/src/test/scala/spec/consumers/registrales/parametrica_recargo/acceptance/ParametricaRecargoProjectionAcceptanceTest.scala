package spec.consumers.registrales.parametrica_recargo.acceptance

import cassandra.CqlSessionSingleton
import cassandra.read.CassandraReadProduction
import cassandra.write.CassandraWriteProduction
import com.datastax.oss.driver.api.core.CqlSession
import spec.consumers.registrales.parametrica_recargo.ParametricaRecargoProjectionSpec

object ParametricaRecargoProjectionAcceptanceTest {

  val testContext: ParametricaRecargoProjectionSpec.TestContext = {
    implicit val session: CqlSession = CqlSessionSingleton.session
    ParametricaRecargoProjectionSpec.TestContext(
      write = new CassandraWriteProduction,
      read = new CassandraReadProduction {
        override def getRow(id: String): Option[Map[String, String]] = {
          super.getRow(s"select * from read_side.buc_param_recargo;")
        }
      }
    )
  }

}

class ParametricaRecargoProjectionAcceptanceTest
    extends ParametricaRecargoProjectionSpec(
      _ => ParametricaRecargoProjectionAcceptanceTest.testContext
    )
