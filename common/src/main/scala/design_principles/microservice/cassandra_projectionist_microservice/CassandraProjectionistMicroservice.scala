package design_principles.microservice.cassandra_projectionist_microservice

import akka.actor.ActorSystem
import akka.entity.ShardedEntity
import api.actor_transaction.ActorTransaction
import design_principles.microservice.Microservice
import kafka.KafkaMessageProcessorRequirements

abstract class CassandraProjectionistMicroservice(
    implicit
    m: CassandraProjectionistMicroserviceRequirements
) extends Microservice[CassandraProjectionistMicroserviceRequirements] {

  def actorTransactions: Set[ActorTransaction[_]]

  implicit val kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements =
    m.kafkaMessageProcessorRequirements
  implicit val system: ActorSystem = m.ctx
  implicit val monitoringAndCassandraWrite: ShardedEntity.MonitoringAndCassandraWrite =
    m.monitoringAndCassandraWrite

}
