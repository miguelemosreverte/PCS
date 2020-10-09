package spec.consumers.registrales.subasta.unit_test

import design_principles.projection.mock.CassandraTestkitMock
import spec.consumers.registrales.subasta.SubastaProjectionSpec

object SubastaProjectionUnitTest {

  private val cassandraTestkit: CassandraTestkitMock = new CassandraTestkitMock()

  val testContext: SubastaProjectionSpec.TestContext = SubastaProjectionSpec.TestContext(
    write = cassandraTestkit.cassandraWrite,
    read = cassandraTestkit.cassandraRead
  )
}

class SubastaProjectionUnitTest
    extends SubastaProjectionSpec(
      _ => SubastaProjectionUnitTest.testContext
    )
