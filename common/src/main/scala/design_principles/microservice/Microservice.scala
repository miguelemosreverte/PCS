package design_principles.microservice

import akka.http.scaladsl.server.Route

trait Microservice {
  def route(m: MicroserviceRequirements): Route
}
