package akka.projections.cassandra

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.projections.cassandra.CassandraProjectionist.CassandraProjectionistRequirements
import akka.projections.{ProjectionHandler, ProjectionSettings}
import akka.stream.alpakka.cassandra.CassandraSessionSettings
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSession, CassandraSessionRegistry}
import cassandra.write.{CassandraWrite, CassandraWriteProduction}
import scalaz.concurrent.Task.Try

abstract class CassandraProjectionHandler[T](settings: ProjectionSettings, system: ActorSystem[_])
    extends ProjectionHandler[T](settings, system) {

  private val sessionSettings = CassandraSessionSettings.create()
  private implicit val session: CassandraSession = CassandraSessionRegistry.get(system).sessionFor(sessionSettings)

  val cassandra: CassandraWrite = new CassandraWriteProduction()

  def run(): Unit =
    CassandraProjectionist.startProjection(
      CassandraProjectionistRequirements(
        projectionSettings = settings,
        system = system,
        projectionHandler = this
      )
    )

  def route: Route =
    path("api" / "projection" / settings.name / "start") {
      complete {
        Try {
          run()
        }.toEither match {
          case Left(e) =>
            HttpResponse(InternalServerError, entity = e.getMessage)
          case Right(value) =>
            StatusCodes.OK
        }
      }
    }

}

object CassandraProjectionHandler {
  case class CassandraProjectionHandlerRequirements(
      settings: ProjectionSettings,
      system: ActorSystem[_]
  )
}
