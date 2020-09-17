package readside.proyectionists.no_registrales.obligacion
import akka.entity.ShardedEntity.MonitoringAndCassandraWrite

import scala.concurrent.Future
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import cassandra.write.CassandraWriteProduction
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionPersistedSnapshot
import design_principles.actor_model.Response.SuccessProcessing
import design_principles.actor_model.Response
import monitoring.Monitoring
import readside.proyectionists.no_registrales.obligacion.projectionists.ObligacionSnapshotProjection

class ObligacionPersistedSnapshotHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[ObligacionPersistedSnapshot](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "ObligacionPersistedSnapshot"

  import consumers.no_registral.obligacion.infrastructure.json._

  override def processInput(input: String): Either[Throwable, ObligacionPersistedSnapshot] =
    serialization
      .maybeDecode[ObligacionPersistedSnapshot](input)

  override def processMessage(registro: ObligacionPersistedSnapshot): Future[Response.SuccessProcessing] = {
    val projection = ObligacionSnapshotProjection(registro)
    for {
      done <- r.cassandraWrite writeState projection
    } yield SuccessProcessing(registro.deliveryId)
  }

}
