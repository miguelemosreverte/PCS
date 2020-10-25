package spec.consumers.registrales.etapas_procesales.unit_test

import design_principles.projection.mock.CassandraTestkitMock
import spec.consumers.registrales.etapas_procesales.EtapasProcesalesProjectionSpec

object EtapasProcesalesProjectionUnitTest {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock()

  val testContext: EtapasProcesalesProjectionSpec.TestContext = EtapasProcesalesProjectionSpec.TestContext(
    write = cassandraTestkit.cassandraWrite,
    read = cassandraTestkit.cassandraRead
  )
}

class EtapasProcesalesProjectionUnitTest
    extends EtapasProcesalesProjectionSpec(
      _ => EtapasProcesalesProjectionUnitTest.testContext
    )
