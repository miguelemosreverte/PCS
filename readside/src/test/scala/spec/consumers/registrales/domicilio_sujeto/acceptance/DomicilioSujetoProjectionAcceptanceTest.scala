package spec.consumers.registrales.domicilio_sujeto.acceptance

import cassandra.CqlSessionSingleton
import cassandra.read.CassandraReadProduction
import cassandra.write.CassandraWriteProduction
import com.datastax.oss.driver.api.core.CqlSession
import spec.consumers.registrales.domicilio_sujeto.DomicilioSujetoProjectionSpec

object DomicilioSujetoProjectionAcceptanceTest {

  val testContext: DomicilioSujetoProjectionSpec.TestContext = {
    implicit val session: CqlSession = CqlSessionSingleton.session
    DomicilioSujetoProjectionSpec.TestContext(
      write = new CassandraWriteProduction,
      read = new CassandraReadProduction {
        override def getRow(id: String): Option[Map[String, String]] = {
          super.getRow(s"select * from read_side.buc_domicilios_sujeto;")
        }
      }
    )
  }

}

class DomicilioSujetoProjectionAcceptanceTest
    extends DomicilioSujetoProjectionSpec(
      _ => DomicilioSujetoProjectionAcceptanceTest.testContext
    )
