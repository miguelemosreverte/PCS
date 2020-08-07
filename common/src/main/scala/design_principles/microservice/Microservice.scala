package design_principles.microservice

import akka.http.scaladsl.server.Route

trait Microservice[Requirements <: MicroserviceRequirements] {
  def route(context: Requirements): Route
}
