package spec

import org.slf4j.{Logger, LoggerFactory}
import spec.CassandraHelper.readTable
import utils.implicits.MapT._

object ProyectionistSpecUtils {

  type Row = Map[String, String]
  type Table = Seq[Map[String, String]]
  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def validateTable(tableName: String, testName: String)(findRow: Table => Option[Row])(compareTo: Row): Unit = {
    val table: Table = readTable(tableName)

    println(s"${Console.CYAN} Validating $tableName | $testName ${Console.RESET}")
    assert(findRow(table) isDefined)

    log.info(table.toString())
    findRow(table) foreach (row => println(row.prettyPrint))

    val row: Row = findRow(table).get

    compareTo foreach {
      case (key, value) =>
        log.info(s"row($key) == value" + " | " + s"${row(key)} == $value")
        assert(row(key) == value)
    }
  }

  def validateTableNotExists(tableName: String, testName: String)(findRow: Table => Option[Row]): Unit = {
    val table: Table = readTable(tableName)

    table foreach (row => println(row.prettyPrint))

    println(s"${Console.CYAN} Validating non existence of $tableName | $testName ${Console.RESET}")
    assert(findRow(table).isEmpty)

  }
}
