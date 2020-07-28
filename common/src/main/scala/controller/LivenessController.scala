package controller

import play.api.libs.json.{JsObject, JsString}

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.http.scaladsl.server.Directives._

import monitoring.Monitoring

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
