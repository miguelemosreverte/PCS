package spec.consumers.registrales.parametrica_plan.unit_test

import design_principles.projection.mock.CassandraTestkitMock
import spec.consumers.registrales.parametrica_plan.ParametricaPlanProjectionSpec

object ParametricaPlanProjectionUnitTest {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock()

  val testContext: ParametricaPlanProjectionSpec.TestContext = ParametricaPlanProjectionSpec.TestContext(
    write = cassandraTestkit.cassandraWrite,
    read = cassandraTestkit.cassandraRead
  )
}

class ParametricaPlanProjectionUnitTest
    extends ParametricaPlanProjectionSpec(
      _ => ParametricaPlanProjectionUnitTest.testContext
    )
