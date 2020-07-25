package akka.projections

import akka.actor.typed.ActorSystem
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.projection.ProjectionId
import akka.projection.cassandra.scaladsl.{AtLeastOnceCassandraProjection, CassandraProjection}
import akka.projection.eventsourced.EventEnvelope
import akka.projection.eventsourced.scaladsl.EventSourcedProvider

object ProjectionFactory {
  def createProjectionFor[T](
      system: ActorSystem[_],
      projectionId: String,
      projectionHandler: ProjectionHandler[T]
  ): AtLeastOnceCassandraProjection[EventEnvelope[T]] = {
    val tag = projectionHandler.settings.tag
    val sourceProvider = EventSourcedProvider
      .eventsByTag[T](system = system, readJournalPluginId = CassandraReadJournal.Identifier, tag = tag)
    CassandraProjection.atLeastOnce(projectionId = ProjectionId(projectionId, tag),
                                    sourceProvider,
                                    handler = projectionHandler)
  }
}
