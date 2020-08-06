package design_principles.microservice.context_provider

import akka.actor.typed.scaladsl.ActorContext
import akka.cluster.ClusterEvent.MemberUp
import akka.http.scaladsl.server.Route
import design_principles.dependency_injection.ContextProvider2
import design_principles.microservice.MicroserviceRequirements

trait MicroserviceContextProvider
    extends ContextProvider2[
      ActorContext[MemberUp],
      MicroserviceRequirements,
      Route,
      Route
    ] {
  def getContext(requirements: ActorContext[MemberUp])(visitor: MicroserviceRequirements => Route): Route
}
