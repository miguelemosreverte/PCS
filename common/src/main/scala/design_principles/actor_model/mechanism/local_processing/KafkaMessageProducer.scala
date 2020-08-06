package design_principles.actor_model.mechanism.local_processing

import org.apache.kafka.clients.producer.ProducerRecord

object KafkaMessageProducer {
  def producerRecord(topic: String,
                     nr_partitions: Int,
                     entityId: String,
                     message: String): ProducerRecord[String, String] = {
    val shardAndPartition = LocalizedProcessingMessageExtractor.shardAndPartition(entityId, nr_partitions)
    new ProducerRecord[String, String](topic, shardAndPartition, entityId, message)
  }

}
