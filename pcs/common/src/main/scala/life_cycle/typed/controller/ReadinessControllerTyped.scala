package life_cycle.typed.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import life_cycle.controller.Controller
import life_cycle.typed.AppLifecycleTyped

import scala.concurrent.ExecutionContext

final class ReadinessControllerTyped(appLifecycle: AppLifecycleTyped)(implicit ec: ExecutionContext)
    extends Controller {

  override val exceptionHandler = ExceptionHandler {
    case error =>
      log.error("Error in Readiness Controller", error)
      complete(StatusCodes.InternalServerError)
  }

  val route: Route = get {
    path("api" / "system" / "ready") {
      handleErrors(exceptionHandler) {
        complete {
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
