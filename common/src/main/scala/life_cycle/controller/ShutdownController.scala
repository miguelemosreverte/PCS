package life_cycle.controller

import akka.http.Controller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import life_cycle.AppLifecycle
import monitoring.Monitoring
import org.slf4j.Logger
import play.api.libs.json.{JsObject, JsString}

final class ShutdownController(appLifecycle: AppLifecycle, logger: Logger, monitoring: Monitoring)
    extends Controller(monitoring) {

  override val exceptionHandler = ExceptionHandler {
    case _ =>
      criticals.increment()
      complete(StatusCodes.InternalServerError)
  }

  val route: Route = post {
    path("api" / "system" / "shutdown") {
      handleErrors(exceptionHandler) {
        requests.increment()
        logger.warn("shutting down rules engine application")
        appLifecycle.shutdown()
        complete(JsObject(Map("status" -> JsString("shutting_down"))))
      }
    }
  }
}
