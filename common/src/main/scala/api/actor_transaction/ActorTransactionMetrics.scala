package api.actor_transaction

import akka.Done
import akka.http.scaladsl.server.Route
import akka.pattern.AskTimeoutException
import design_principles.actor_model.Response
import monitoring.{Counter, Histogram, Monitoring}
import org.slf4j.LoggerFactory
import serialization.SerializationError

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

abstract class ActorTransactionMetrics(
    monitoring: Monitoring
)(implicit ec: ExecutionContext) {

  final private val metricPrefix = "actor-transaction"
  final private val controllerId = api.Utils.Transformation.to_underscore(this.getClass.getSimpleName)
  final protected val requests: Counter = monitoring.counter(s"$metricPrefix-$controllerId-request")
  final protected val errors: Counter = monitoring.counter(s"$metricPrefix-$controllerId-error")
  final protected val latency: Histogram = monitoring.histogram(s"$metricPrefix-$controllerId-latency")

  private final val log = LoggerFactory.getLogger(this.getClass)

  final protected def recordRequests(future: Future[Response.SuccessProcessing]): Unit =
    requests.increment()
  final protected def recordLatency(future: Future[Response.SuccessProcessing]): Unit =
    latency.recordFuture(future)
  final protected def recordErrors(future: Future[Response.SuccessProcessing]): Unit =
    future onComplete {
      case Success(_) => ()
      case Failure(e) =>
        e match {
          case e: SerializationError =>
            errors.increment()
            log.error(e.getMessage)
          case e: AskTimeoutException =>
            errors.increment()
            log.error(e.getMessage)
          case unexpectedException: Throwable =>
            errors.increment()
            log.error(unexpectedException.getMessage)
        }
    }

}
