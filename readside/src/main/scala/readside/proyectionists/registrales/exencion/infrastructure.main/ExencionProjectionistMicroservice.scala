package readside.proyectionists.registrales.exencion.infrastructure.main

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import design_principles.microservice.cassandra_projectionist_microservice.{
  CassandraProjectionistMicroservice,
  CassandraProjectionistMicroserviceRequirements
}
import readside.proyectionists.registrales.exencion.ObjetoAddedExencionHandler

class ExencionProjectionistMicroservice(
    implicit m: CassandraProjectionistMicroserviceRequirements
) extends CassandraProjectionistMicroservice {

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      new ObjetoAddedExencionHandler
    )

  override def route: Route =
    actorTransactions.map(_.route) reduce (_ ~ _)

}
