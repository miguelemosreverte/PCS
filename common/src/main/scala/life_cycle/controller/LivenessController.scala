package life_cycle.controller

import akka.http.Controller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import monitoring.Monitoring
import play.api.libs.json.{JsObject, JsString}

final class LivenessController(monitoring: Monitoring) extends Controller(monitoring) {

  override val exceptionHandler = ExceptionHandler {
    case _ =>
      criticals.increment()
      complete(StatusCodes.InternalServerError)
  }

  val route: Route = get {
    path("api" / "system" / "status") {
      requests.increment()
      handleErrors(exceptionHandler) {
        complete(JsObject(Map("status" -> JsString("ok"))))
      }
    }
  }
}
