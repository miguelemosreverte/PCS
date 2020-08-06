package no_registrales

import akka.actor.ActorSystem
import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryTestKit
import consumers_spec.no_registrales.testsuite.NoRegistralesTestSuite
import design_principles.actor_model.ActorSpecCassandra
import design_principles.projection.CassandraTestkit

trait BaseE2ESpec extends NoRegistralesTestSuite {
  abstract class BaseE2ETestContext(implicit val system: ActorSystem) extends TestContext with ActorSpecCassandra {
    def cassandraTestkit: CassandraTestkit
    def Query: NoRegistralesQueryTestKit
    def close(): Unit
  }
}
