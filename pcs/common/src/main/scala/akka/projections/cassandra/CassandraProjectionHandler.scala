package akka.projections.cassandra

import akka.actor.typed.ActorSystem
import akka.projections.{ProjectionHandler, ProjectionSettings}
import akka.stream.alpakka.cassandra.CassandraSessionSettings
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSession, CassandraSessionRegistry}
import cassandra.write.{CassandraWrite, CassandraWriteProduction}

abstract class CassandraProjectionHandler[T](settings: ProjectionSettings, system: ActorSystem[_])
    extends ProjectionHandler[T](settings, system) {

  private val sessionSettings = CassandraSessionSettings.create()
  private implicit val session: CassandraSession = CassandraSessionRegistry.get(system).sessionFor(sessionSettings)

  val cassandra: CassandraWrite = new CassandraWriteProduction()
}
