package consumers_spec.no_registrales.testkit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import akka.Done
import akka.actor.ActorRef
import consumers.no_registral.cotitularidad.infrastructure.kafka.{
  AddCotitularTransaction,
  CotitularPublishSnapshotTransaction
}
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.infrastructure.consumer._
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto.ObligacionesTri
import consumers.no_registral.obligacion.infrastructure.consumer.{
  ObligacionNoTributariaTransaction,
  ObligacionTributariaTransaction
}
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto
import consumers.no_registral.sujeto.infrastructure.consumer.{
  SujetoNoTributarioTransaction,
  SujetoTributarioTransaction
}
import consumers_spec.no_registrales.testsuite.ToJson._
import design_principles.external_pub_sub.kafka.KafkaMock.MessageProcessorImplicits
import kafka.{MessageProcessor, MessageProducer}

class MessageTestkitUtils(sujeto: ActorRef, cotitularidadActor: ActorRef, messageProducer: MessageProducer) {

  implicit class StartMessageProcessor(messageProcessor: MessageProcessor) {
    def startProcessing(): Unit = {
      // COTITULARIDAD CONSUMERS
      messageProcessor.subscribeActorTransaction(
        SOURCE_TOPIC = "AddCotitularTransaction",
        AddCotitularTransaction()(cotitularidadActor)
      )
      messageProcessor.subscribeActorTransaction(
        SOURCE_TOPIC = "CotitularidadPublishSnapshot",
        CotitularPublishSnapshotTransaction()(cotitularidadActor)
      )
      // OBJETO CONSUMERS
      messageProcessor.subscribeActorTransaction(
        SOURCE_TOPIC = "DGR-COP-EXENCIONES",
        ObjetoExencionTransaction()(sujeto)
      )
      messageProcessor.subscribeActorTransaction(
        SOURCE_TOPIC = "DGR-COP-OBJETOS-ANT",
        ObjetoNoTributarioTransaction()(sujeto)
      )
      messageProcessor.subscribeActorTransaction(
        SOURCE_TOPIC = "DGR-COP-OBJETOS-TRI",
        ObjetoTributarioTransaction()(sujeto)
      )
      messageProcessor.subscribeActorTransaction(
        SOURCE_TOPIC = "ObjetoUpdateCotitularesTransaction",
        ObjetoUpdateCotitularesTransaction()(sujeto)
      )
      messageProcessor.subscribeActorTransaction(
        SOURCE_TOPIC = "ObjetoReceiveSnapshot",
        ObjetoUpdateNovedadTransaction()(sujeto)
      )
      // OBLIGACION CONSUMERS
      messageProcessor.subscribeActorTransaction(
        "DGR-COP-OBLIGACIONES-ANT",
        ObligacionNoTributariaTransaction()(sujeto)
      )
      messageProcessor.subscribeActorTransaction(
        "DGR-COP-OBLIGACIONES-TRI",
        ObligacionTributariaTransaction()(sujeto)
      )
      // SUJETO CONSUMERS
      messageProcessor.subscribeActorTransaction(
        "DGR-COP-SUJETO-TRI",
        SujetoTributarioTransaction()(sujeto)
      )
      messageProcessor.subscribeActorTransaction(
        "DGR-COP-SUJETO-ANT",
        SujetoNoTributarioTransaction()(sujeto)
      )
      ()
    }
  }

}

object MessageTestkitUtils {
  implicit class MessageProducerNoRegistrales(messageProducer: MessageProducer) {
    import consumers_spec.no_registrales.testsuite.ToJson._
    def produceObligacion(obligacion: ObligacionExternalDto): Future[Done] = {
      val topic = obligacion match {
        case _: ObligacionExternalDto.ObligacionesAnt => "DGR-COP-OBLIGACIONES-ANT"
        case _: ObligacionExternalDto.ObligacionesTri => "DGR-COP-OBLIGACIONES-TRI"
      }
      messageProducer.produce(Seq(obligacion.toJson), topic)(_ => ())
    }

    def produceObjeto(objeto: ObjetoExternalDto): Future[Done] = {
      val topic = objeto match {
        case _: ObjetoExternalDto.ObjetosAnt => "DGR-COP-OBJETOS-ANT"
        case _: ObjetoExternalDto.ObjetosTri => "DGR-COP-OBJETOS-TRI"
      }
      messageProducer.produce(Seq(objeto.toJson), topic)(_ => ())
    }

    def produceSujeto(sujeto: SujetoExternalDto): Future[Done] = {
      val topic = sujeto match {
        case _: SujetoExternalDto.SujetoAnt => "DGR-COP-SUJETO-ANT"
        case _: SujetoExternalDto.SujetoTri => "DGR-COP-SUJETO-TRI"
      }
      messageProducer.produce(Seq(sujeto.toJson), topic)(_ => ())
    }
  }
}
