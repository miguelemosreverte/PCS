package readside.proyectionists.no_registrales.objeto
import akka.entity.ShardedEntity.MonitoringAndCassandraWrite

import scala.concurrent.{ExecutionContext, Future}
import api.actor_transaction.ActorTransaction
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import design_principles.actor_model.Response.SuccessProcessing
import design_principles.actor_model.Response
import readside.proyectionists.no_registrales.objeto.projections.ObjetoSnapshotPersistedProjection

class ObjetoSnapshotPersistedHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[ObjetoSnapshotPersisted](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "ObjetoSnapshotPersistedReadside"

  override def processInput(input: String): Either[Throwable, ObjetoSnapshotPersisted] = {
    import consumers.no_registral.objeto.infrastructure.json._
    serialization
      .maybeDecode[ObjetoSnapshotPersisted](input)
  }

  override def processMessage(registro: ObjetoSnapshotPersisted): Future[Response.SuccessProcessing] = {
    val projection = ObjetoSnapshotPersistedProjection(registro)
    for {
      done <- r.cassandraWrite writeState projection
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }

}
