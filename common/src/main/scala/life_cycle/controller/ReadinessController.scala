package life_cycle.controller

import akka.http.Controller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import life_cycle.AppLifecycle
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

final class ReadinessController(appLifecycle: AppLifecycle, monitoring: Monitoring)(implicit ec: ExecutionContext)
    extends Controller(monitoring) {

  override val exceptionHandler = ExceptionHandler {
    case _ =>
      criticals.increment()
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
