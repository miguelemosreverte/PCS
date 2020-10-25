package spec.consumers.registrales.juicio.acceptance

import cassandra.CqlSessionSingleton
import cassandra.read.CassandraReadProduction
import cassandra.write.CassandraWriteProduction
import com.datastax.oss.driver.api.core.CqlSession
import spec.consumers.registrales.juicio.JuicioProjectionSpec

object JuicioProjectionAcceptanceTest {

  val testContext: JuicioProjectionSpec.TestContext = {
    implicit val session: CqlSession = CqlSessionSingleton.session
    JuicioProjectionSpec.TestContext(
      write = new CassandraWriteProduction,
      read = new CassandraReadProduction {
        override def getRow(id: String): Option[Map[String, String]] = {
          super.getRow(s"select * from read_side.buc_juicios;")
        }
      }
    )
  }

}

class JuicioProjectionAcceptanceTest
    extends JuicioProjectionSpec(
      _ => JuicioProjectionAcceptanceTest.testContext
    )
