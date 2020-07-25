package consumers_spec.no_registrales.cotitularidad

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.application.entities.ObjetoResponses.GetObjetoResponse
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.application.entity.SujetoResponses.GetSujetoResponse
import consumers_spec.no_registrales.testkit.Examples
import consumers_spec.no_registrales.testsuite.NoRegistralesTestSuite
import consumers_spec.no_registrales.testsuite.ToJson._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import stubs.consumers.no_registrales.common._
import utils.generators.Model.deliveryId

trait CotitularidadSpec extends NoRegistralesTestSuite {

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
    val context = testContext()

    context.messageProducer.produce(
      List(
        cotitularidad(examples.objeto2)(examples.sujetoId1, "S").toJson
      ),
      "DGR-COP-OBJETOS-TRI"
    )(_ => ())

    eventually {
      context.messageProcessor.messageHistory.last match {
        case (topic, _) if topic == "AddCotitularTransaction" => true
      }
    }

    context.messageProducer.produce(
      List(
        cotitularidad(examples.objeto2)(examples.sujetoId2, "N").toJson
      ),
      "DGR-COP-OBJETOS-TRI"
    )(_ => ())

    eventually {
      val response: GetObjetoResponse = context.Query getStateObjeto ObjetoMessageRoots(examples.sujetoId1,
                                                                                        examples.objetoId2._1,
                                                                                        examples.objetoId2._2)
      response.sujetos should be(Set(examples.sujetoId1, examples.sujetoId2))
      log.info(response.prettyPrint)
    }

    eventually {
      val response: GetObjetoResponse = context.Query getStateObjeto ObjetoMessageRoots(examples.sujetoId2,
                                                                                        examples.objetoId2._1,
                                                                                        examples.objetoId2._2)
      response.sujetos should be(Set(examples.sujetoId1, examples.sujetoId2))
      log.info(response.prettyPrint)
    }

    context.messageProducer.produce(
      List(
        examples.obligacionWithSaldo50.toJson
      ),
      "DGR-COP-OBLIGACIONES-TRI"
    )(_ => ())

    eventually {
      val response: GetObjetoResponse = context.Query getStateObjeto ObjetoMessageRoots(examples.sujetoId1,
                                                                                        examples.objetoId2._1,
                                                                                        examples.objetoId2._2)
      response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO)
      log.info(response.prettyPrint)
    }

    eventually {
      val response: GetObjetoResponse = context.Query getStateObjeto ObjetoMessageRoots(examples.sujetoId2,
                                                                                        examples.objetoId2._1,
                                                                                        examples.objetoId2._2)
      response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO)
      log.info(response.prettyPrint)
    }

    eventually {
      val response: GetSujetoResponse = context.Query getStateSujeto SujetoMessageRoots(examples.sujetoId1)
      response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO)
      log.info(response.prettyPrint)
    }

    eventually {
      val response: GetSujetoResponse = context.Query getStateSujeto SujetoMessageRoots(examples.sujetoId2)
      response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO)
      log.info(response.prettyPrint)
    }

    context.messageProcessor.messageHistory(0)._1 should be("DGR-COP-OBJETOS-TRI")
    context.messageProcessor.messageHistory(1)._1 should be("AddCotitularTransaction")
    context.messageProcessor.messageHistory(2)._1 should be("DGR-COP-OBJETOS-TRI")
    context.messageProcessor.messageHistory(3)._1 should be("AddCotitularTransaction")
    context.messageProcessor
      .messageHistory(4)
      ._1 should be("ObjetoUpdateCotitularesTransaction") // contains two messages one for each objeto which informs about the cotitulares Set(1,2) // TODO should only send a AddCotitular to objeto 1
    context.messageProcessor
      .messageHistory(5)
      ._1 should be("ObjetoUpdateCotitularesTransaction") // contains two messages one for each objeto which informs about the cotitulares Set(1,2) // TODO should only send a AddCotitular to objeto 1
    context.messageProcessor.messageHistory(6)._1 should be("CotitularidadPublishSnapshot")
    context.messageProcessor
      .messageHistory(7)
      ._1 should be("ObjetoReceiveSnapshot") // TODO separar mensajes informOfCurrentState
    context.messageProcessor.messageHistory(8)._1 should be("DGR-COP-OBLIGACIONES-TRI")
    context.messageProcessor.messageHistory(9)._1 should be("CotitularidadPublishSnapshot")
    context.messageProcessor
      .messageHistory(10)
      ._1 should be("ObjetoReceiveSnapshot") // contains two messages one for each objeto which informs about the cotitulares Set(1,2)
    context.messageProcessor.messageHistory.size should be(11)

    context.close()
  }

}
