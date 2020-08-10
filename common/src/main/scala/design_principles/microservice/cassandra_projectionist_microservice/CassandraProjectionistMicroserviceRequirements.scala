package design_principles.microservice.cassandra_projectionist_microservice

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.cluster.ClusterEvent.MemberUp
import design_principles.microservice.MicroserviceRequirements
import kafka.KafkaMessageProcessorRequirements
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

case class CassandraProjectionistMicroserviceRequirements(
    monitoring: Monitoring,
    executionContext: ExecutionContext,
    ctx: ActorSystem
) extends MicroserviceRequirements
