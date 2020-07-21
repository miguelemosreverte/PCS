package life_cycle.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsArray, JsObject, JsString}

abstract class Controller extends PlayJsonSupport {

  def route: Route
  val exceptionHandler: ExceptionHandler

  def handleErrors(exceptionHandler: ExceptionHandler): Directive[Unit] =
    handleExceptions(exceptionHandler) & handleRejections(rejectionsHandler)

  private val rejectionsHandler: RejectionHandler = RejectionHandler
    .newBuilder()
    .handle {
      case ex: ValidationRejection =>
        val errorMsg = "validation rejection has occurred: " + ex.message
        complete((StatusCodes.BadRequest, JsObject(Map("errors" -> JsArray(Seq(JsString(errorMsg)))))))
      case ex: MalformedRequestContentRejection =>
        val errorMsg = "malformed request has occurred: " + ex.message
        complete((StatusCodes.BadRequest, JsObject(Map("errors" -> JsArray(Seq(JsString(errorMsg)))))))
    }
    .result()

  val log = LoggerFactory.getLogger(this.getClass)

}
