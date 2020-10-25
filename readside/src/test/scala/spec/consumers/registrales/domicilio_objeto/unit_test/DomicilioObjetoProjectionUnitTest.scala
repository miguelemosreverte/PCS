package spec.consumers.registrales.domicilio_objeto.unit_test

import design_principles.projection.mock.CassandraTestkitMock
import spec.consumers.registrales.domicilio_objeto.DomicilioObjetoProjectionSpec

object DomicilioObjetoProjectionUnitTest {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock()

  val testContext: DomicilioObjetoProjectionSpec.TestContext = DomicilioObjetoProjectionSpec.TestContext(
    write = cassandraTestkit.cassandraWrite,
    read = cassandraTestkit.cassandraRead
  )
}

class DomicilioObjetoProjectionUnitTest
    extends DomicilioObjetoProjectionSpec(
      _ => DomicilioObjetoProjectionUnitTest.testContext
    )
