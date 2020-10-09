package spec.consumers.registrales.exencion.unit_test

import design_principles.projection.mock.CassandraTestkitMock
import spec.consumers.registrales.exencion.ExencionProjectionSpec

object ExencionProjectionUnitTest {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock()

  val testContext: ExencionProjectionSpec.TestContext = ExencionProjectionSpec.TestContext(
    write = cassandraTestkit.cassandraWrite,
    read = cassandraTestkit.cassandraRead
  )
}

class ExencionProjectionUnitTest
    extends ExencionProjectionSpec(
      _ => ExencionProjectionUnitTest.testContext
    )
