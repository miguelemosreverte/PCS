package cassandra.read

trait CassandraRead {
  def getRow(cql: String): Option[Map[String, String]]
}
