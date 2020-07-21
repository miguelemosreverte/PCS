package consumers_spec.no_registrales.testsuite

import akka.actor.ActorSystem
import akka.persistence.query.NoOffset
import akka.projection.eventsourced.EventEnvelope
import consumers_spec.no_registrales.testkit.NoRegistralesImplicitConversions
import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryTestKit
import design_principles.actor_model.{ActorSpec, ActorSpecWriteside}

trait NoRegistralesTestSuite extends ActorSpec with NoRegistralesImplicitConversions {

  def testContext()(implicit system: ActorSystem): TestContext
  abstract class TestContext(implicit system: ActorSystem) extends ActorSpecWriteside {
    def Query: NoRegistralesQueryTestKit
    def close(): Unit
  }
}

object NoRegistralesTestSuite {
  def eventEnvelope[Event](event: Event): EventEnvelope[Event] =
    EventEnvelope[Event](NoOffset, "", 1L, event, utils.generators.Model.deliveryId)
}
