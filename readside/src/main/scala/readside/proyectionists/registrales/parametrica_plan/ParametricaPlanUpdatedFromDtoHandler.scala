package readside.proyectionists.registrales.parametrica_plan
import akka.entity.ShardedEntity.MonitoringAndCassandraWrite

import scala.concurrent.Future
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import cassandra.write.CassandraWriteProduction
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents.ParametricaPlanUpdatedFromDto
import design_principles.actor_model.Response.SuccessProcessing
import design_principles.actor_model.Response
import monitoring.Monitoring
import readside.proyectionists.registrales.parametrica_plan.projections.ParametricaPlanUpdatedFromDtoProjection

class ParametricaPlanUpdatedFromDtoHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[ParametricaPlanUpdatedFromDto](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "ParametricaPlanUpdatedFromDto"

  import consumers.registral.parametrica_plan.infrastructure.json._

  override def processInput(input: String): Either[Throwable, ParametricaPlanUpdatedFromDto] =
    serialization
      .maybeDecode[ParametricaPlanUpdatedFromDto](input)

  val cassandra = new CassandraWriteProduction()
  override def processMessage(registro: ParametricaPlanUpdatedFromDto): Future[Response.SuccessProcessing] = {
    val projection = ParametricaPlanUpdatedFromDtoProjection(registro)
    for {
      done <- cassandra writeState projection
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }

}
