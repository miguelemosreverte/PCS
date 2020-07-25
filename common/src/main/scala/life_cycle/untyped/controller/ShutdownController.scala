package life_cycle.untyped.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import life_cycle.controller.Controller
import life_cycle.untyped.AppLifecycle
import play.api.libs.json.{JsObject, JsString}

final class ShutdownController(appLifecycle: AppLifecycle) extends Controller {

  override val exceptionHandler = ExceptionHandler {
    case error =>
      log.error("Error in Shutdown Controller", error)
      complete(StatusCodes.InternalServerError)
  }

  val route: Route = post {
    path("api" / "system" / "shutdown") {
      handleErrors(exceptionHandler) {
        log.warn("shutting down rules engine application")
        appLifecycle.shutdown()
        complete(JsObject(Map("status" -> JsString("shutting_down"))))
      }
    }
  }
}
