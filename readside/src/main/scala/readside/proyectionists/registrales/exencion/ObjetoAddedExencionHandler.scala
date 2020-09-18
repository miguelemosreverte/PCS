package readside.proyectionists.registrales.exencion
import akka.entity.ShardedEntity.MonitoringAndCassandraWrite

import scala.concurrent.Future
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import cassandra.write.CassandraWriteProduction
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoAddedExencion
import design_principles.actor_model.Response.SuccessProcessing
import design_principles.actor_model.Response
import monitoring.Monitoring
import readside.proyectionists.registrales.exencion.projections.ObjetoAddedExencionProjection

class ObjetoAddedExencionHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[ObjetoAddedExencion](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "ObjetoAddedExencion"

  import consumers.no_registral.objeto.infrastructure.json._

  override def processInput(input: String): Either[Throwable, ObjetoAddedExencion] =
    serialization
      .maybeDecode[ObjetoAddedExencion](input)

  val cassandra = new CassandraWriteProduction()
  override def processMessage(registro: ObjetoAddedExencion): Future[Response.SuccessProcessing] = {
    val projection = ObjetoAddedExencionProjection(registro)
    for {
      done <- cassandra writeState projection
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }

}
