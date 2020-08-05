package generators.consumers.no_registrales

import java.util.concurrent.atomic.AtomicInteger

import ddd.ExternalDto
import generators.consumers.no_registrales.Generator.KafkaKeyValue

trait Generator[E <: ExternalDto] {
  val i: AtomicInteger = new AtomicInteger()
  def example: E
  def next: E

  final def nextKafkaKeyValue: KafkaKeyValue = {
    val e = next
    KafkaKeyValue(
      aggregateRoot = aggregateRoot(e),
      json = toJson(e)
    )
  }
  def toJson(e: E): String
  def aggregateRoot(e: E): String

}
object Generator {
  case class KafkaKeyValue(aggregateRoot: String, json: String) {
    def key = aggregateRoot
    def value = json
  }
}
