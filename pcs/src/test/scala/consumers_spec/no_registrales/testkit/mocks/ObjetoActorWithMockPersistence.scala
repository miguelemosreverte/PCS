package consumers_spec.no_registrales.testkit.mocks

import scala.concurrent.Future
import akka.Done
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorSystem, Props}
import akka.projections.ProjectionSettings
import consumers.no_registral.objeto.domain.{ObjetoEvents, ObjetoState}
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor.ObjetoTags
import consumers.no_registral.objeto.infrastructure.event_processor.ObjetoNovedadCotitularidadProjectionHandler
import kafka.MessageProducer
import monitoring.{DummyMonitoring, Monitoring}

class ObjetoActorWithMockPersistence(
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
    val obligacionProps = new ObligacionActorWithMockPersistence().props(monitoring)
    Props(
      new ObjetoActor(monitoring, Some(obligacionProps)) {

        override def persistSnapshotForAllCotitulares(evt: ObjetoEvents, consolidatedState: ObjetoState)(
            handler: () => Unit = () => ()
        ): Unit =
          objetoUpdateNovedadCotitularidadThatUsesKafkaMock.processEvent(evt)

        override def persistEvent(event: ObjetoEvents, tags: Set[String])(handler: () => Unit): Unit = {
          super.persistEvent(event, tags)(handler)
          if (ObjetoTags.CotitularesReadside.subsetOf(tags)) {
            objetoUpdateNovedadCotitularidadThatUsesKafkaMock.processEvent(event)
          }
        }

      }
    )
  }
}
