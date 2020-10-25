package spec.consumers.registrales.declaracion_jurada.unit_test

import design_principles.projection.mock.CassandraTestkitMock
import spec.consumers.registrales.declaracion_jurada.DeclaracionJuradaProjectionSpec

object DeclaracionJuradaProjectionUnitTest {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock()

  val testContext: DeclaracionJuradaProjectionSpec.TestContext = DeclaracionJuradaProjectionSpec.TestContext(
    write = cassandraTestkit.cassandraWrite,
    read = cassandraTestkit.cassandraRead
  )
}

class DeclaracionJuradaProjectionUnitTest
    extends DeclaracionJuradaProjectionSpec(
      _ => DeclaracionJuradaProjectionUnitTest.testContext
    )
