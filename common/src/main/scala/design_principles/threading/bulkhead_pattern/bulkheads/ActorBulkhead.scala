package design_principles.threading.bulkhead_pattern.bulkheads

import design_principles.threading.bulkhead_pattern.BulkheadPattern

import scala.concurrent.ExecutionContext

case class ActorBulkhead() extends BulkheadPattern {
  def executionContext: ExecutionContext = BulkheadPattern(BulkheadPattern.halfOfTheAvailableThreads)
}