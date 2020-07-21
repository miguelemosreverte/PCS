package design_principles.actor_model.testsuite

import design_principles.actor_model.{ActorSpec, ActorSpecWriteside}
import design_principles.actor_model.testkit.{InfrastructureTestkit, QueryTestkit}

trait ActorE2ETestSuite extends ActorSpec with ActorSpecWriteside with InfrastructureTestkit {

  override val Query: QueryTestkit.AgainstHTTP
}
