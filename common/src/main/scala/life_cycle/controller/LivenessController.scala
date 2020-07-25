package life_cycle.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import play.api.libs.json.{JsObject, JsString}

final class LivenessController extends Controller {

  override val exceptionHandler = ExceptionHandler {
    case error =>
      log.error("Error in Liveness Controller", error)
      complete(StatusCodes.InternalServerError)
  }

  val route: Route = get {
    path("api" / "system" / "status") {
      handleErrors(exceptionHandler) {
        complete(JsObject(Map("status" -> JsString("ok"))))
      }
    }
  }
}
