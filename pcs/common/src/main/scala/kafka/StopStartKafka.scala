package kafka

import api.actor_transaction.ActorTransaction

object StopStartKafka {

  case class StartStopKafkaRequirements(actorTransactions: ActorTransaction,
                                        transactionRequirements: KafkaMessageProcessorRequirements)

  private type Topic = String
}
