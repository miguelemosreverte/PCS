package consumers_spec.no_registrales.cotitularidad

import akka.actor.ActorSystem
import consumers.no_registral.cotitularidad.application.entities.CotitularidadMessage.CotitularidadMessageRoots
import consumers.no_registral.cotitularidad.application.entities.CotitularidadResponses.GetCotitularesResponse
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.application.entities.ObjetoResponses.GetObjetoResponse
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.application.entity.SujetoResponses.GetSujetoResponse
import consumers_spec.no_registrales.sujeto.SujetoSpec
import consumers_spec.no_registrales.testkit.{Examples, NoRegistralesImplicitConversions}
import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryTestKit
import consumers_spec.no_registrales.testsuite.ToJson._
import design_principles.actor_model.ActorSpec
import design_principles.actor_model.testkit.QueryTestkit.AgainstActors
import design_principles.external_pub_sub.kafka.MessageProcessorLogging
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.{MessageProcessor, MessageProducer}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import stubs.consumers.no_registrales.common._
import utils.generators.Model.deliveryId

object CotitularidadSpec {
  case class TestContext(messageProducer: MessageProducer,
                         messageProcessor: MessageProcessor with MessageProcessorLogging,
                         Query: NoRegistralesQueryTestKit)
}
abstract class CotitularidadSpec(
    getContext: ActorSystem => CotitularidadSpec.TestContext
) extends ActorSpec
    with NoRegistralesImplicitConversions {

  val examples = new Examples("CotitularidadSpec")

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def cotitularidad(objeto: ObjetosTri)(sujetoId: String, responsable: String): ObjetosTri =
    objeto.copy(
      SOJ_OTROS_ATRIBUTOS = Some(Json.parse(s"""
             {
             "SOJ_DETALLES" : [ {
                          "RESPONSABLE_OTROS_ATRIBUTOS" : "$responsable",
                          "PORCENTAJE_OTROS_ATRIBUTOS" : "50",
                          "OTROS_ATRIBUTOS_ADHERIDO_DEBITO" : "N",
                          "CUENTA_SOJ_OTROS_ATRIBUTOS" : null,
                          "PERIODO_SOJ_OTROS_ATRIBUTOS" : null,
                          "IMPORTE_SOJ_OTROS_ATRIBUTOS" : null
                        } ]
             }
            """)),
      SOJ_SUJ_IDENTIFICADOR = sujetoId,
      EV_ID = deliveryId
    )

  "the shared objeto" should " know the sujetos that share its pocession" in parallelActorSystemRunner { implicit s =>
    val context = getContext(s)
    val messageProcessor = context.messageProcessor
    val messageProducer = context.messageProducer
    val Query = context.Query
    messageProducer.produce(
      Seq(
        KafkaKeyValue(
          aggregateRoot = s"Sujeto-${examples.sujetoId1}-Objeto-${examples.objetoId2}-Tipo-I",
          json = cotitularidad(examples.objeto2)(examples.sujetoId1, "S").toJson
        )
      ),
      "DGR-COP-OBJETOS-TRI"
    )(_ => ())

    eventually {
      val response: GetCotitularesResponse = Query getStateCotitularidad CotitularidadMessageRoots(
          examples.objetoId2._1,
          examples.objetoId2._2
        )
      response.sujetoResponsable should be(examples.sujetoId1)
      log.info(response.prettyPrint)
    }

    messageProducer.produce(
      Seq(
        KafkaKeyValue(
          aggregateRoot = s"Sujeto-${examples.sujetoId2}-Objeto-${examples.objetoId2}-Tipo-I",
          json = cotitularidad(examples.objeto2)(examples.sujetoId2, "N").toJson
        )
      ),
      "DGR-COP-OBJETOS-TRI"
    )(_ => ())

    eventually {
      val response: GetCotitularesResponse = Query getStateCotitularidad CotitularidadMessageRoots(
          examples.objetoId2._1,
          examples.objetoId2._2
        )
      response.sujetosCotitulares + response.sujetoResponsable should be(Set(examples.sujetoId1, examples.sujetoId2))
      log.info(response.prettyPrint)
    }

    messageProducer.produce(
      Seq(
        KafkaKeyValue(
          aggregateRoot = examples.obligacionWithSaldo50.toObligacionAggregateRoot.toString,
          json = examples.obligacionWithSaldo50.toJson
        )
      ),
      "DGR-COP-OBLIGACIONES-TRI"
    )(_ => ())

    eventually {
      val response: GetObjetoResponse = Query getStateObjeto ObjetoMessageRoots(
          examples.sujetoId1,
          examples.objetoId2._1,
          examples.objetoId2._2
        )
      response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO)
      log.info(response.prettyPrint)
    }

    eventually {
      val response: GetObjetoResponse = Query getStateObjeto ObjetoMessageRoots(
          examples.sujetoId2,
          examples.objetoId2._1,
          examples.objetoId2._2
        )

      response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO)
      log.info(response.prettyPrint)
    }

    eventually {
      val response: GetSujetoResponse = Query getStateSujeto SujetoMessageRoots(examples.sujetoId1)
      response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO)
      log.info(response.prettyPrint)
    }

    eventually {
      val response: GetSujetoResponse = Query getStateSujeto SujetoMessageRoots(examples.sujetoId2)
      response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO)
      log.info(response.prettyPrint)
    }

    messageProcessor.messageHistory.foreach { m =>
      println(m._1)
    }

    messageProcessor.messageHistory.map { m =>
      m._1
    } should be(
      Seq(
        "DGR-COP-OBJETOS-TRI", // message A for objeto A, yes responsible
        "ObjetoSnapshotPersisted", // thanks to message A
        "DGR-COP-OBJETOS-TRI", // message B for objeto B, not responsible
        "ObjetoSnapshotPersisted", // thanks to message B
        "DGR-COP-OBLIGACIONES-TRI", // message C
        "ObjetoSnapshotPersisted", // thanks to message C
        "ObjetoReceiveSnapshot", // message D -- generated by CotitularidadActor
        // CotitularidadActor informs objeto B
        // of the message C
        // which arrived at objeto A
        "ObjetoSnapshotPersisted" // thanks to message D
      )
    )

  }

}
