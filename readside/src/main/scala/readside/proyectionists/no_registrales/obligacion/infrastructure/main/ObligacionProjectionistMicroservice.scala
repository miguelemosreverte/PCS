package readside.proyectionists.no_registrales.obligacion.infrastructure.main

import akka.http.scaladsl.server.Route
import design_principles.microservice.cassandra_projectionist_microservice.{CassandraProjectionistMicroservice, CassandraProjectionistMicroserviceRequirements}
import readside.proyectionists.no_registrales.obligacion.ObligacionProjectionHandler

object ObligacionProjectionistMicroservice extends CassandraProjectionistMicroservice {
  override def route(context: CassandraProjectionistMicroserviceRequirements): Route = {
    val monitoring = context.monitoring

    import akka.actor.typed.scaladsl.adapter._
    val system = context.ctx.toTyped
    val projectionist = ObligacionProjectionHandler(monitoring, system)
    projectionist.run()
    projectionist.route
  }
}

/*
application
  - handler -- seria como el controller HTTP (estamos haciendo todo en el controller )
dominio
  - persistentSnapshot*/
