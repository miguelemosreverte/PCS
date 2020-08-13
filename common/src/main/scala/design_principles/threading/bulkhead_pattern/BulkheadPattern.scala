package design_principles.threading.bulkhead_pattern

import java.util.concurrent.ForkJoinPool

import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

trait BulkheadPattern {
  def executionContext: ExecutionContext
}
object BulkheadPattern {
  val globalParallelism = {
    val config = ConfigFactory.load()
    val forkJoinExecutorConfig = config.getConfig("akka.actor.default-dispatcher.fork-join-executor")
    val globalParallelismMax = forkJoinExecutorConfig.getInt("parallelism-max")
    globalParallelismMax
  }
  val halfOfTheAvailableThreads = globalParallelism / 2

  def apply(maxThreads: Int) =
    ExecutionContext.fromExecutor(new ForkJoinPool(maxThreads))

}
