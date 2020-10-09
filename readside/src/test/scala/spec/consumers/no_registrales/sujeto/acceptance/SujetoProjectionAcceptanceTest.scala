package spec.consumers.no_registrales.sujeto.acceptance

import cassandra.CqlSessionSingleton
import cassandra.read.CassandraReadProduction
import cassandra.write.CassandraWriteProduction
import com.datastax.oss.driver.api.core.CqlSession
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import spec.consumers.no_registrales.sujeto.SujetoProjectionSpec

object SujetoProjectionAcceptanceTest {

  val testContext: SujetoProjectionSpec.TestContext = {
    implicit val session: CqlSession = CqlSessionSingleton.session
    SujetoProjectionSpec.TestContext(
      write = new CassandraWriteProduction,
      read = new CassandraReadProduction {
        override def getRow(id: String): Option[Map[String, String]] = {
          val keys = SujetoMessageRoots.extractor(id)
          super.getRow(
            s"select * from read_side.buc_sujeto" +
            s" where " +
            s"suj_identificador='${keys.sujetoId}' " +
            s"ALLOW FILTERING;"
          )
        }
      }
    )
  }

}

class SujetoProjectionAcceptanceTest
    extends SujetoProjectionSpec(
      _ => SujetoProjectionAcceptanceTest.testContext
    )
