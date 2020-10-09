package spec.consumers.registrales.juicio.unit_test

import design_principles.projection.mock.CassandraTestkitMock
import spec.consumers.registrales.juicio.JuicioProjectionSpec

object JuicioProjectionUnitTest {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock()

  val testContext: JuicioProjectionSpec.TestContext = JuicioProjectionSpec.TestContext(
    write = cassandraTestkit.cassandraWrite,
    read = cassandraTestkit.cassandraRead
  )
}

class JuicioProjectionUnitTest
    extends JuicioProjectionSpec(
      _ => JuicioProjectionUnitTest.testContext
    )
