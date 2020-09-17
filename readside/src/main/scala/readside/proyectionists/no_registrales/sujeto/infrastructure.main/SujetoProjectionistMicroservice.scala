package readside.proyectionists.no_registrales.sujeto.infrastructure.main

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import design_principles.microservice.cassandra_projectionist_microservice.{
  CassandraProjectionistMicroservice,
  CassandraProjectionistMicroserviceRequirements
}
import readside.proyectionists.no_registrales.sujeto.SujetoSnapshotPersistedHandler

class SujetoProjectionistMicroservice(
    implicit m: CassandraProjectionistMicroserviceRequirements
) extends CassandraProjectionistMicroservice {

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      new SujetoSnapshotPersistedHandler
    )

  override def route: Route =
    actorTransactions.map(_.route) reduce (_ ~ _)

}
