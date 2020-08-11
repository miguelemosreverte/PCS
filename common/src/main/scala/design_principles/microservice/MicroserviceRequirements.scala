package design_principles.microservice

import scala.concurrent.ExecutionContext

import akka.actor.ActorSystem
import monitoring.Monitoring

trait MicroserviceRequirements {
  def monitoring: Monitoring
  def executionContext: ExecutionContext
  def ctx: ActorSystem
}
