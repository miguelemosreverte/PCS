package akka.entity

import scala.io.Source

object EntityIdOps {

  object AggregateRootCandidates {
    val separatedByUpperLine: String = "11-22"
    val justString: String = "Foo22"
    val numeric: String = "22"
    val uuid: String = "00000000-0000-0000-C000-000000000046"
  }

  val mapping: Map[Char, Int] =
    Source
      .fromResource("UTF-8-character-table")
      .getLines
      .toSeq
      .map(_ trim)
      .map(_(0))
      .zipWithIndex
      .toMap

  def encode(aggregateRootCandidate: String): String =
    aggregateRootCandidate
      .map { letter =>
        mapping(letter)
      }
      .map(_ toString)
      .reduce(_ + "|" + _)

  def decode(encoded: String): String =
    encoded
      .split('|')
      .map(_ toInt)
      .map(mapping.map(_ swap)(_))
      .map(_ toString)
      .reduce(_ + _)

  def fromEncodedToBigInt(encoded: String): BigInt = BigInt(encoded.split('|').mkString(""))

}
