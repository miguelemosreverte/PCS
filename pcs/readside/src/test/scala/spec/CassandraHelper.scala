package spec

import org.slf4j.LoggerFactory
import utils.implicits.Bash._

object CassandraHelper {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def readTable(tableName: String): Seq[Map[String, String]] = {
    val outputStream: Seq[String] = s"docker exec cassandra cqlsh -e 'select * from $tableName'".bash
      .filterNot(_.isEmpty)
      .toIndexedSeq

    def getRowColumns(row: String): Array[String] = row.split("\\|").map(_.trim)

    val tableHeader = getRowColumns(outputStream head)
    val tableRows = outputStream.tail.tail.init map getRowColumns map {
      tableHeader zip _
    }
    tableRows map {
      _ toMap
    }
  }
}
