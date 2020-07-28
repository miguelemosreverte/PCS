package controller

import scala.concurrent.ExecutionContext
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.http.scaladsl.server.Directives._
import monitoring.Monitoring
import life_cycle.AppLifecycle

final class ReadinessController(appLifecycle: AppLifecycle, monitoring: Monitoring)(implicit ec: ExecutionContext)
    extends Controller(monitoring) {

  override val exceptionHandler = ExceptionHandler {
    case _ =>
      errors.increment()
      complete(StatusCodes.InternalServerError)
  }

  val route: Route = get {
    path("api" / "system" / "ready") {
      requests.increment()
      handleErrors(exceptionHandler) {
        complete {
          latency.recordFuture {
            appLifecycle
              .isAppReady()
              .map {
                case true => StatusCodes.OK
                case false => StatusCodes.NotFound
              }
          }
        }
      }
    }
  }
}
