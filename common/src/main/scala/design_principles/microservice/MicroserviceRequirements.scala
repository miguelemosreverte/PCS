package design_principles.microservice

import akka.actor.typed.scaladsl.ActorContext
import akka.cluster.ClusterEvent.MemberUp
import monitoring.Monitoring
import scala.concurrent.ExecutionContext

trait MicroserviceRequirements {
  def monitoring: Monitoring
  def executionContext: ExecutionContext
  def ctx: ActorContext[MemberUp]
}
