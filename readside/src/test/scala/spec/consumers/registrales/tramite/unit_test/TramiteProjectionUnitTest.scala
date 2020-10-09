package spec.consumers.registrales.tramite.unit_test

import design_principles.projection.mock.CassandraTestkitMock
import spec.consumers.registrales.tramite.TramiteProjectionSpec

object TramiteProjectionUnitTest {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock()

  val testContext: TramiteProjectionSpec.TestContext = TramiteProjectionSpec.TestContext(
    write = cassandraTestkit.cassandraWrite,
    read = cassandraTestkit.cassandraRead
  )
}

class TramiteProjectionUnitTest
    extends TramiteProjectionSpec(
      _ => TramiteProjectionUnitTest.testContext
    )
