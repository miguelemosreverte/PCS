package spec.consumers.registrales.domicilio_sujeto.unit_test

import design_principles.projection.mock.CassandraTestkitMock
import spec.consumers.registrales.domicilio_sujeto.DomicilioSujetoProjectionSpec

object DomicilioSujetoProjectionUnitTest {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock()

  val testContext: DomicilioSujetoProjectionSpec.TestContext = DomicilioSujetoProjectionSpec.TestContext(
    write = cassandraTestkit.cassandraWrite,
    read = cassandraTestkit.cassandraRead
  )
}

class DomicilioSujetoProjectionUnitTest
    extends DomicilioSujetoProjectionSpec(
      _ => DomicilioSujetoProjectionUnitTest.testContext
    )
