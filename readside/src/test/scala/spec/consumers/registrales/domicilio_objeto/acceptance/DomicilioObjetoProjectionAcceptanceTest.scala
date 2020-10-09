package spec.consumers.registrales.domicilio_objeto.acceptance

import cassandra.CqlSessionSingleton
import cassandra.read.CassandraReadProduction
import cassandra.write.CassandraWriteProduction
import com.datastax.oss.driver.api.core.CqlSession
import spec.consumers.registrales.domicilio_objeto.DomicilioObjetoProjectionSpec

object DomicilioObjetoProjectionAcceptanceTest {

  val testContext: DomicilioObjetoProjectionSpec.TestContext = {
    implicit val session: CqlSession = CqlSessionSingleton.session
    DomicilioObjetoProjectionSpec.TestContext(
      write = new CassandraWriteProduction,
      read = new CassandraReadProduction {
        override def getRow(id: String): Option[Map[String, String]] = {
          super.getRow(s"select * from read_side.buc_domicilios_objeto;")
        }
      }
    )
  }

}

class DomicilioObjetoProjectionAcceptanceTest
    extends DomicilioObjetoProjectionSpec(
      _ => DomicilioObjetoProjectionAcceptanceTest.testContext
    )
