package controller

import play.api.libs.json.{JsArray, JsObject, JsString}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import monitoring.{Counter, Histogram, KamonMonitoring, Monitoring}

abstract class Controller(monitoring: Monitoring = new KamonMonitoring) extends PlayJsonSupport {

  def route: Route
  val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case _ =>
      criticals.increment()
      complete(StatusCodes.InternalServerError)
  }

  private val metricPrefix = "controller"
  private val controllerId = api.Utils.Transformation.to_underscore(this.getClass.getSimpleName)
  protected val warnings: Counter = monitoring.counter(s"$metricPrefix-$controllerId-warning")
  protected val criticals: Counter = monitoring.counter(s"$metricPrefix-$controllerId-critical")
  protected val requests: Counter = monitoring.counter(s"$metricPrefix-$controllerId-request")
  protected val latency: Histogram = monitoring.histogram(s"$metricPrefix-$controllerId-latency")

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
}
