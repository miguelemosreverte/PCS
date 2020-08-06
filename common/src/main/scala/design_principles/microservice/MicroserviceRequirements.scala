package design_principles.microservice

import akka.actor.typed.scaladsl.ActorContext
import akka.cluster.ClusterEvent.MemberUp
import kafka.KafkaMessageProcessorRequirements
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

case class MicroserviceRequirements(
    monitoring: Monitoring,
    executionContext: ExecutionContext,
    ctx: ActorContext[MemberUp],
    kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements
)
