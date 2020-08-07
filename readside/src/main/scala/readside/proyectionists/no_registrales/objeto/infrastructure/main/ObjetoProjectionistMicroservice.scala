package readside.proyectionists.no_registrales.objeto.infrastructure.main

import akka.http.scaladsl.server.Route
import design_principles.microservice.cassandra_projectionist_microservice._
import readside.proyectionists.no_registrales.objeto.ObjetoProjectionHandler

object ObjetoProjectionistMicroservice extends CassandraProjectionistMicroservice {
  override def route(context: CassandraProjectionistMicroserviceRequirements): Route = {
    val monitoring = context.monitoring
    val system = context.ctx.system
    val projectionist = ObjetoProjectionHandler(monitoring, system)
    projectionist.run()
    projectionist.route
  }
}
