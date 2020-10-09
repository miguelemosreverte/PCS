package spec.consumers.registrales.plan_pago.acceptance

import cassandra.CqlSessionSingleton
import cassandra.read.CassandraReadProduction
import cassandra.write.CassandraWriteProduction
import com.datastax.oss.driver.api.core.CqlSession
import spec.consumers.registrales.plan_pago.PlanPagoProjectionSpec

object PlanPagoProjectionAcceptanceTest {

  val testContext: PlanPagoProjectionSpec.TestContext = {
    implicit val session: CqlSession = CqlSessionSingleton.session
    PlanPagoProjectionSpec.TestContext(
      write = new CassandraWriteProduction,
      read = new CassandraReadProduction {
        override def getRow(id: String): Option[Map[String, String]] = {
          super.getRow(s"select * from read_side.buc_planes_pago;")
        }
      }
    )
  }

}

class PlanPagoProjectionAcceptanceTest
    extends PlanPagoProjectionSpec(
      _ => PlanPagoProjectionAcceptanceTest.testContext
    )
