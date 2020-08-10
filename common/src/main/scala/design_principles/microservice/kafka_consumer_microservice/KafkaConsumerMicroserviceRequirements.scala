package design_principles.microservice.kafka_consumer_microservice

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.cluster.ClusterEvent.MemberUp
import design_principles.microservice.MicroserviceRequirements
import kafka.KafkaMessageProcessorRequirements
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

case class KafkaConsumerMicroserviceRequirements(
    monitoring: Monitoring,
    executionContext: ExecutionContext,
    ctx: ActorSystem,
    kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements
) extends MicroserviceRequirements
