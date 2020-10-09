package spec.consumers.registrales.subasta.acceptance

import cassandra.CqlSessionSingleton
import cassandra.read.CassandraReadProduction
import cassandra.write.CassandraWriteProduction
import com.datastax.oss.driver.api.core.CqlSession
import spec.consumers.registrales.subasta.SubastaProjectionSpec

object SubastaProjectionAcceptanceTest {

  val testContext: SubastaProjectionSpec.TestContext = {
    implicit val session: CqlSession = CqlSessionSingleton.session
    SubastaProjectionSpec.TestContext(
      write = new CassandraWriteProduction,
      read = new CassandraReadProduction {
        override def getRow(id: String): Option[Map[String, String]] = {
          super.getRow(s"select * from read_side.buc_subastas;")
        }
      }
    )
  }

}

class SubastaProjectionAcceptanceTest
    extends SubastaProjectionSpec(
      _ => SubastaProjectionAcceptanceTest.testContext
    )
