package spec.consumers.no_registrales.sujeto.unit_test

import design_principles.projection.mock.CassandraTestkitMock
import spec.consumers.no_registrales.sujeto.SujetoProjectionSpec

object SujetoProjectionUnitTest {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock()

  val testContext: SujetoProjectionSpec.TestContext = SujetoProjectionSpec.TestContext(
    write = cassandraTestkit.cassandraWrite,
    read = cassandraTestkit.cassandraRead
  )
}

class SujetoProjectionUnitTest
    extends SujetoProjectionSpec(
      _ => SujetoProjectionUnitTest.testContext
    )
