package no_registrales.mocks

import akka.Done
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorSystem, Props}
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import akka.projections.ProjectionSettings
import config.MockConfig
import consumers.no_registral.objeto.domain.{ObjetoEvents, ObjetoState}
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor.ObjetoTags
import consumers.no_registral.objeto.infrastructure.event_processor.ObjetoNovedadCotitularidadProjectionHandler
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers_spec.no_registrales.testsuite.NoRegistralesTestSuite.eventEnvelope
import kafka.MessageProducer
import monitoring.{DummyMonitoring, Monitoring}

import scala.concurrent.Future

class ObjetoActorWithMockPersistence(
    obligacionProyectionist: Handler[EventEnvelope[ObligacionEvents]],
    objetoProyectionist: Handler[EventEnvelope[ObjetoEvents]],
    validateReadside: Any => Any,
    messageProducer: MessageProducer
)(implicit system: ActorSystem) {
  val objetoSettings = ProjectionSettings("ObjetoNovedadCotitularidad", 1, new DummyMonitoring)

  val objetoUpdateNovedadCotitularidadThatUsesKafkaMock: ObjetoNovedadCotitularidadProjectionHandler =
    new ObjetoNovedadCotitularidadProjectionHandler(objetoSettings, system.toTyped) {
      override def publishMessageToKafka(messages: Seq[String], topic: String): Future[Done] = {
        messageProducer.produce(messages, topic)(_ => ())
        Future(Done)
      }
    }
  def props(monitoring: Monitoring): Props = {
    val obligacionProps =
      new ObligacionActorWithMockPersistence(validateReadside, obligacionProyectionist).props(monitoring)
    Props(
      new ObjetoActor(monitoring, Some(obligacionProps), MockConfig.config) {

        override def persistSnapshotForAllCotitulares(evt: ObjetoEvents, consolidatedState: ObjetoState)(
            handler: () => Unit = () => ()
        ): Unit =
          objetoUpdateNovedadCotitularidadThatUsesKafkaMock.processEvent(evt)

        override def persistEvent(event: ObjetoEvents, tags: Set[String])(handler: () => Unit): Unit = {
          super.persistEvent(event, tags)(handler)
          if (ObjetoTags.CotitularesReadside.subsetOf(tags)) {
            objetoUpdateNovedadCotitularidadThatUsesKafkaMock.processEvent(event)
          }
          if (ObjetoTags.ObjetoReadside subsetOf tags) {
            objetoProyectionist.process(eventEnvelope(event))
            validateReadside(event)
          }
        }

      }
    )
  }
}
