package spec.consumers.registrales.parametrica_recargo.unit_test

import design_principles.projection.mock.CassandraTestkitMock
import spec.consumers.registrales.parametrica_recargo.ParametricaRecargoProjectionSpec

object ParametricaRecargoProjectionUnitTest {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock()

  val testContext: ParametricaRecargoProjectionSpec.TestContext = ParametricaRecargoProjectionSpec.TestContext(
    write = cassandraTestkit.cassandraWrite,
    read = cassandraTestkit.cassandraRead
  )
}

class ParametricaRecargoProjectionUnitTest
    extends ParametricaRecargoProjectionSpec(
      _ => ParametricaRecargoProjectionUnitTest.testContext
    )
