package spec.consumers.no_registrales.obligacion.unit_test

import design_principles.projection.mock.CassandraTestkitMock
import spec.consumers.no_registrales.obligacion.ObligacionProjectionSpec

object ObligacionProjectionUnitTest {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock()

  val testContext: ObligacionProjectionSpec.TestContext = ObligacionProjectionSpec.TestContext(
    write = cassandraTestkit.cassandraWrite,
    read = cassandraTestkit.cassandraRead
  )
}

class ObligacionProjectionUnitTest
    extends ObligacionProjectionSpec(
      _ => ObligacionProjectionUnitTest.testContext
    )
