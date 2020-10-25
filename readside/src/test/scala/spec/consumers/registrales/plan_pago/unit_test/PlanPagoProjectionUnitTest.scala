package spec.consumers.registrales.plan_pago.unit_test

import design_principles.projection.mock.CassandraTestkitMock
import spec.consumers.registrales.plan_pago.PlanPagoProjectionSpec

object PlanPagoProjectionUnitTest {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock()

  val testContext: PlanPagoProjectionSpec.TestContext = PlanPagoProjectionSpec.TestContext(
    write = cassandraTestkit.cassandraWrite,
    read = cassandraTestkit.cassandraRead
  )
}

class PlanPagoProjectionUnitTest
    extends PlanPagoProjectionSpec(
      _ => PlanPagoProjectionUnitTest.testContext
    )
