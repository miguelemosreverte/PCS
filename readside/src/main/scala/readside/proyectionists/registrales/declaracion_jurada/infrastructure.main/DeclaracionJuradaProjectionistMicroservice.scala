package readside.proyectionists.registrales.declaracion_jurada.infrastructure.main

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import design_principles.microservice.cassandra_projectionist_microservice.{
  CassandraProjectionistMicroservice,
  CassandraProjectionistMicroserviceRequirements
}
import readside.proyectionists.registrales.declaracion_jurada.DeclaracionJuradaUpdatedFromDtoHandler

class DeclaracionJuradaProjectionistMicroservice(
    implicit m: CassandraProjectionistMicroserviceRequirements
) extends CassandraProjectionistMicroservice {

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      new DeclaracionJuradaUpdatedFromDtoHandler
    )

  override def route: Route =
    actorTransactions.map(_.route) reduce (_ ~ _)

}
