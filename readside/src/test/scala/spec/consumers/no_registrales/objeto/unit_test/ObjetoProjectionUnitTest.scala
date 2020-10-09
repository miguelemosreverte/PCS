package spec.consumers.no_registrales.objeto.unit_test

import design_principles.projection.mock.CassandraTestkitMock
import spec.consumers.no_registrales.objeto.ObjetoProjectionSpec

object ObjetoProjectionUnitTest {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock()

  val testContext: ObjetoProjectionSpec.TestContext = ObjetoProjectionSpec.TestContext(
    write = cassandraTestkit.cassandraWrite,
    read = cassandraTestkit.cassandraRead
  )
}

class ObjetoProjectionUnitTest
    extends ObjetoProjectionSpec(
      _ => ObjetoProjectionUnitTest.testContext
    )
