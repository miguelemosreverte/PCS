package readside.proyectionists.no_registrales.sujeto
import akka.entity.ShardedEntity.MonitoringAndCassandraWrite

import scala.concurrent.Future
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import cassandra.write.CassandraWriteProduction
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoSnapshotPersisted
import design_principles.actor_model.Response.SuccessProcessing
import design_principles.actor_model.Response
import monitoring.Monitoring
import readside.proyectionists.no_registrales.sujeto.projections.SujetoSnapshotPersistedProjection

class SujetoSnapshotPersistedHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[SujetoSnapshotPersisted](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "SujetoSnapshotPersisted"

  import consumers.no_registral.sujeto.infrastructure.json._

  override def processInput(input: String): Either[Throwable, SujetoSnapshotPersisted] =
    serialization
      .maybeDecode[SujetoSnapshotPersisted](input)

  val cassandra = new CassandraWriteProduction()
  override def processMessage(registro: SujetoSnapshotPersisted): Future[Response.SuccessProcessing] = {
    val projection = SujetoSnapshotPersistedProjection(registro)
    for {
      done <- cassandra writeState projection
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }

}
