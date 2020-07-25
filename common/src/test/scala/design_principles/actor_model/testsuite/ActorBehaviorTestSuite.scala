package design_principles.actor_model.testsuite

import design_principles.actor_model.{ActorSpec, ActorSpecWriteside}
import design_principles.actor_model.testkit.{MockTestkit, QueryTestkit}

trait ActorBehaviorTestSuite extends ActorSpec with ActorSpecWriteside with MockTestkit {

  override val Query: QueryTestkit.AgainstActors
}
