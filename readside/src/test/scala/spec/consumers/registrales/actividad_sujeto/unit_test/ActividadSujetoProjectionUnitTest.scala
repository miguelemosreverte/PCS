package spec.consumers.registrales.actividad_sujeto.unit_test

import design_principles.projection.mock.CassandraTestkitMock
import spec.consumers.registrales.actividad_sujeto.ActividadSujetoProjectionSpec

object ActividadSujetoProjectionUnitTest {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock()

  val testContext: ActividadSujetoProjectionSpec.TestContext = ActividadSujetoProjectionSpec.TestContext(
    write = cassandraTestkit.cassandraWrite,
    read = cassandraTestkit.cassandraRead
  )
}

class ActividadSujetoProjectionUnitTest
    extends ActividadSujetoProjectionSpec(
      _ => ActividadSujetoProjectionUnitTest.testContext
    )
