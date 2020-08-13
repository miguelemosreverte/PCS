package consumers_spec.no_registrales.testkit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorRef
import consumers.no_registral.cotitularidad.infrastructure.kafka.{
  AddCotitularTransaction,
  CotitularPublishSnapshotTransaction
}
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.infrastructure.consumer._
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto
import consumers.no_registral.obligacion.infrastructure.consumer.{
  ObligacionNoTributariaTransaction,
  ObligacionTributariaTransaction
}
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto
import consumers.no_registral.sujeto.infrastructure.consumer.{
  SujetoNoTributarioTransaction,
  SujetoTributarioTransaction
}
import design_principles.actor_model.Response
import design_principles.external_pub_sub.kafka.KafkaMock.MessageProcessorImplicits
import kafka.{MessageProcessor, MessageProducer}
import monitoring.DummyMonitoring

class MessageTestkitUtils(sujeto: ActorRef, cotitularidadActor: ActorRef, messageProducer: MessageProducer) {
  val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit class StartMessageProcessor(messageProcessor: MessageProcessor) {
    val monitoring = new DummyMonitoring
    def startProcessing(): Unit = {
      // COTITULARIDAD CONSUMERS
      messageProcessor.subscribeActorTransaction(
        SOURCE_TOPIC = "AddCotitularTransaction",
        AddCotitularTransaction(cotitularidadActor, monitoring)(ec)
      )
      messageProcessor.subscribeActorTransaction(
        SOURCE_TOPIC = "CotitularidadPublishSnapshot",
        CotitularPublishSnapshotTransaction(cotitularidadActor, monitoring)(ec)
      )
      // OBJETO CONSUMERS
      messageProcessor.subscribeActorTransaction(
        SOURCE_TOPIC = "DGR-COP-EXENCIONES",
        ObjetoExencionTransaction(sujeto, monitoring)(ec)
      )
      messageProcessor.subscribeActorTransaction(
        SOURCE_TOPIC = "DGR-COP-OBJETOS-ANT",
        ObjetoNoTributarioTransaction(sujeto, monitoring)(ec)
      )
      messageProcessor.subscribeActorTransaction(
        SOURCE_TOPIC = "DGR-COP-OBJETOS-TRI",
        ObjetoTributarioTransaction(sujeto, monitoring)(ec)
      )
      messageProcessor.subscribeActorTransaction(
        SOURCE_TOPIC = "ObjetoUpdateCotitularesTransaction",
        ObjetoUpdateCotitularesTransaction(sujeto, monitoring)(ec)
      )
      messageProcessor.subscribeActorTransaction(
        SOURCE_TOPIC = "ObjetoReceiveSnapshot",
        ObjetoUpdateNovedadTransaction(sujeto, monitoring)(ec)
      )
      // OBLIGACION CONSUMERS
      messageProcessor.subscribeActorTransaction(
        "DGR-COP-OBLIGACIONES-ANT",
        ObligacionNoTributariaTransaction(sujeto, monitoring)(ec)
      )
      messageProcessor.subscribeActorTransaction(
        "DGR-COP-OBLIGACIONES-TRI",
        ObligacionTributariaTransaction(sujeto, monitoring)(ec)
      )
      // SUJETO CONSUMERS
      messageProcessor.subscribeActorTransaction(
        "DGR-COP-SUJETO-TRI",
        SujetoTributarioTransaction(sujeto, monitoring)
      )
      messageProcessor.subscribeActorTransaction(
        "DGR-COP-SUJETO-ANT",
        SujetoNoTributarioTransaction(sujeto, monitoring)(ec)
      )
      ()
    }
  }

}

object MessageTestkitUtils {
  implicit class MessageProducerNoRegistrales(messageProducer: MessageProducer) {
    import consumers_spec.no_registrales.testsuite.ToJson._
    def produceObligacion(obligacion: ObligacionExternalDto): Future[akka.Done] = {
      val topic = obligacion match {
        case _: ObligacionExternalDto.ObligacionesAnt => "DGR-COP-OBLIGACIONES-ANT"
        case _: ObligacionExternalDto.ObligacionesTri => "DGR-COP-OBLIGACIONES-TRI"
      }
      messageProducer.produce(Seq(obligacion.toJson), topic)(_ => ())
    }

    def produceObjeto(objeto: ObjetoExternalDto): Future[akka.Done] = {
      val topic = objeto match {
        case _: ObjetoExternalDto.ObjetosAnt => "DGR-COP-OBJETOS-ANT"
        case _: ObjetoExternalDto.ObjetosTri => "DGR-COP-OBJETOS-TRI"
      }
      messageProducer.produce(Seq(objeto.toJson), topic)(_ => ())
    }

    def produceSujeto(sujeto: SujetoExternalDto): Future[akka.Done] = {
      val topic = sujeto match {
        case _: SujetoExternalDto.SujetoAnt => "DGR-COP-SUJETO-ANT"
        case _: SujetoExternalDto.SujetoTri => "DGR-COP-SUJETO-TRI"
      }
      messageProducer.produce(Seq(sujeto.toJson), topic)(_ => ())
    }
  }
}
