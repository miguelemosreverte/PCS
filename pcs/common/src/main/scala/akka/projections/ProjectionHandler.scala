package akka.projections

import akka.actor.typed.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler

abstract class ProjectionHandler[T](val settings: ProjectionSettings, system: ActorSystem[_])
    extends Handler[EventEnvelope[T]] {}
