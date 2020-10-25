package spec.consumers.registrales.declaracion_jurada.acceptance

import cassandra.CqlSessionSingleton
import cassandra.read.CassandraReadProduction
import cassandra.write.CassandraWriteProduction
import com.datastax.oss.driver.api.core.CqlSession
import spec.consumers.registrales.declaracion_jurada.DeclaracionJuradaProjectionSpec

object DeclaracionJuradaProjectionAcceptanceTest {

  val testContext: DeclaracionJuradaProjectionSpec.TestContext = {
    implicit val session: CqlSession = CqlSessionSingleton.session
    DeclaracionJuradaProjectionSpec.TestContext(
      write = new CassandraWriteProduction,
      read = new CassandraReadProduction {
        override def getRow(id: String): Option[Map[String, String]] = {
          super.getRow(s"select * from read_side.buc_declaraciones_juradas;")
        }
      }
    )
  }

}

class DeclaracionJuradaProjectionAcceptanceTest
    extends DeclaracionJuradaProjectionSpec(
      _ => DeclaracionJuradaProjectionAcceptanceTest.testContext
    )
