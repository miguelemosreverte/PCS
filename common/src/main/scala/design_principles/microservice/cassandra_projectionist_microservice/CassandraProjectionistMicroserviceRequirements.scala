package design_principles.microservice.cassandra_projectionist_microservice

import scala.concurrent.ExecutionContext
import akka.actor.ActorSystem
import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import design_principles.microservice.MicroserviceRequirements
import kafka.KafkaMessageProcessorRequirements
import monitoring.Monitoring

case class CassandraProjectionistMicroserviceRequirements(
    monitoring: Monitoring,
    executionContext: ExecutionContext,
    ctx: ActorSystem,
    monitoringAndCassandraWrite: MonitoringAndCassandraWrite,
    kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements
) extends MicroserviceRequirements
