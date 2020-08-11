package design_principles.microservice.cassandra_projectionist_microservice

import scala.concurrent.ExecutionContext

import akka.actor.ActorSystem
import design_principles.microservice.MicroserviceRequirements
import monitoring.Monitoring

case class CassandraProjectionistMicroserviceRequirements(
    monitoring: Monitoring,
    executionContext: ExecutionContext,
    ctx: ActorSystem
) extends MicroserviceRequirements
