package readside.proyectionists.no_registrales.objeto.infrastructure.main

import akka.http.scaladsl.server.Route
import design_principles.microservice.cassandra_projectionist_microservice._
import readside.proyectionists.no_registrales.objeto.ObjetoProjectionHandler

object ObjetoProjectionistMicroservice extends CassandraProjectionistMicroservice {
  override def route(context: CassandraProjectionistMicroserviceRequirements): Route = {
    val monitoring = context.monitoring
    import akka.actor.typed.scaladsl.adapter._
    import akka.actor.typed.scaladsl.adapter._
    val system = context.ctx.toTyped
    val projectionist = ObjetoProjectionHandler(monitoring, system)
    projectionist.run()
    projectionist.route
  }
}
