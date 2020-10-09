package spec.consumers.registrales.actividad_sujeto.acceptance

import cassandra.CqlSessionSingleton
import cassandra.read.CassandraReadProduction
import cassandra.write.CassandraWriteProduction
import com.datastax.oss.driver.api.core.CqlSession
import spec.consumers.registrales.actividad_sujeto.ActividadSujetoProjectionSpec

object ActividadSujetoProjectionAcceptanceTest {

  val testContext: ActividadSujetoProjectionSpec.TestContext = {
    implicit val session: CqlSession = CqlSessionSingleton.session
    ActividadSujetoProjectionSpec.TestContext(
      write = new CassandraWriteProduction,
      read = new CassandraReadProduction {
        override def getRow(id: String): Option[Map[String, String]] = {
          super.getRow(s"select * from read_side.buc_actividades_sujeto;")
        }
      }
    )
  }

}

class ActividadSujetoProjectionAcceptanceTest
    extends ActividadSujetoProjectionSpec(
      _ => ActividadSujetoProjectionAcceptanceTest.testContext
    )
