package registrales.exencion.testkit.mocks

import akka.actor.{ActorSystem, Props}
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import akka.projections.ProjectionSettings
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoAddedExencion
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import consumers_spec.no_registrales.testsuite.NoRegistralesTestSuite.eventEnvelope
import monitoring.{DummyMonitoring, Monitoring}

class ObjetoActorWithMockPersistence(
    exencionProyectionist: Handler[EventEnvelope[ObjetoAddedExencion]]
)(implicit system: ActorSystem) {
  val objetoSettings = ProjectionSettings("ObjetoNovedadCotitularidad", 1, new DummyMonitoring)

  def props(monitoring: Monitoring): Props = {
    val obligacionProps = ObligacionActor.props(monitoring)
    Props(
      new ObjetoActor(monitoring, Some(obligacionProps)) {

        override def persistEvent(event: ObjetoEvents, tags: Set[String])(handler: () => Unit): Unit = {
          super.persistEvent(event, tags)(handler)
          event match {
            case evt: ObjetoEvents.ObjetoAddedExencion =>
              exencionProyectionist.process(eventEnvelope(evt))
            case _ => ()

          }
        }

      }
    )
  }
}
