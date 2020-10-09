package spec.consumers.registrales.parametrica_plan.acceptance

import cassandra.CqlSessionSingleton
import cassandra.read.CassandraReadProduction
import cassandra.write.CassandraWriteProduction
import com.datastax.oss.driver.api.core.CqlSession
import spec.consumers.registrales.parametrica_plan.ParametricaPlanProjectionSpec

object ParametricaPlanProjectionAcceptanceTest {

  val testContext: ParametricaPlanProjectionSpec.TestContext = {
    implicit val session: CqlSession = CqlSessionSingleton.session
    ParametricaPlanProjectionSpec.TestContext(
      write = new CassandraWriteProduction,
      read = new CassandraReadProduction {
        override def getRow(id: String): Option[Map[String, String]] = {
          super.getRow(s"select * from read_side.buc_param_plan;")
        }
      }
    )
  }

}

class ParametricaPlanProjectionAcceptanceTest
    extends ParametricaPlanProjectionSpec(
      _ => ParametricaPlanProjectionAcceptanceTest.testContext
    )
