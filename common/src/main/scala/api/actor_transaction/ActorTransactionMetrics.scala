package api.actor_transaction

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import akka.pattern.AskTimeoutException
import design_principles.actor_model.Response
import monitoring.{Counter, Histogram, Monitoring}
import org.slf4j.LoggerFactory
import serialization.SerializationError

abstract class ActorTransactionMetrics(
    monitoring: Monitoring
) {

  final private val metricPrefix = "actor-transaction"
  final private val controllerId = api.Utils.Transformation.to_underscore(this.getClass.getSimpleName)
  final protected val requests: Counter = monitoring.counter(s"$metricPrefix-$controllerId-request")
  final protected val errors: Counter = monitoring.counter(s"$metricPrefix-$controllerId-error")
  final protected val latency: Histogram = monitoring.histogram(s"$metricPrefix-$controllerId-latency")

  private final val log = LoggerFactory.getLogger(this.getClass)

  final protected def recordRequests(): Unit =
    requests.increment()
  final protected def recordLatency(milliseconds: Long): Unit =
    latency.record(milliseconds)
  final protected def recordErrors(throwable: Throwable): Unit =
    throwable match {
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
