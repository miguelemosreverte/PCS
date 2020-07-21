package consumers_spec.no_registrales.cotitularidad

import scala.concurrent.duration._

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.application.entities.ObjetoQueries.GetStateObjeto
import consumers.no_registral.objeto.application.entities.ObjetoResponses.GetObjetoResponse
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.application.entity.SujetoQueries.GetStateSujeto
import consumers.no_registral.sujeto.application.entity.SujetoResponses.GetSujetoResponse
import consumers_spec.no_registrales.testkit.Examples
import consumers_spec.no_registrales.testsuite.NoRegistralesTestSuite
import consumers_spec.no_registrales.testsuite.ToJson._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import stubs.consumers.no_registrales.common._
import utils.generators.Model.deliveryId

trait CotitularidadSpec extends NoRegistralesTestSuite {
  // @TODO check
  // override def afterEach(): Unit = {
  //   receiveWhile(1 second) { _ =>
  //     ()
  //   }
  // }

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
    context.close()
  }

  """
    objeto 2 is shared by sujeto 1 and sujeto 2. 
    Asking sujeto 1 and sujeto 2 for objeto 2
  """ should "show share the same saldo" in parallelActorSystemRunner { implicit s =>
    val context = testContext()

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
    context.close()
  }

  "sujeto 1 and sujeto 2 " should "show share the same saldo" in parallelActorSystemRunner { implicit s =>
    val context = testContext()

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
    context.close()
  }

  // TODO deserialize the kafkaMessages in order to further document the messages contents
  "The first messages" should
  """
       setup the first objeto
      
       0. The ObjetoTri arrives to the actor, which saves Tagged(ObjetoUpdatedFromTri, Set("ObjetoNovedadCotitularidad"))
       1. ObjetoNovedadCotitularidad, which is an event processor that listens to ObjetoNovedadCotitularidad and gets the ObjetoUpdatedFromTri, publishes AddCotitularTransaction


      Step by step: 
      --> ObjetoTri is sent to DGR-COP-OBJETOS-TRI
      --> ObjetoTributarioTransaction receives ObjetoTri and sends ObjetoUpdateFromTri
      --> ObjetoActor receives ObjetoUpdateFromTri and sends event ObjetoUpdatedFromTri tagged as 'ObjetoNovedadCotitularidad'
      --> ObjetoEventProcessor receives ObjetoUpdateFromTri and sends CotitularidadAddSujetoCotitular to topic 'AddCotitularTransaction'
      --> AddCotitularTransaction receives CotitularidadAddSujetoCotitular and sends CotitularidadAddSujetoCotitular to CotitularidadActor
      --> CotitularidadActor receives CotitularidadAddSujetoCotitular but because there is only 1 cotitular it does not try to inform cotitulares of the new cotitular
      """ in parallelActorSystemRunner { implicit s =>
    val context = testContext()

    context.messageProcessor.messageHistory(0)._1 should be("DGR-COP-OBJETOS-TRI")
    context.messageProcessor.messageHistory(1)._1 should be("AddCotitularTransaction")
    context.close()
  }

  "The second messages group" should """
    set the second objeto, where the first objeto updates the second objeto with the old state via snapshot
    also, the first objeto should get to know the updated cotitulares thanks to CotitularidadActor which sends a message its way

     2. The ObjetoTri arrives to the second objeto actor, which saves Tagged(ObjetoUpdatedFromTri, Set("ObjetoNovedadCotitularidad"))
     3. ObjetoNovedadCotitularidad, which is an event processor that listens to ObjetoNovedadCotitularidad and gets the ObjetoUpdatedFromTri, publishes AddCotitularTransaction
     4. AddCotitularidadTransaction, which is a kafka consumer, gets the AddCotitularidad, and sends an ObjectSnapshot to the topic ObjetoReceiveSnapshot

     Maybe the second objeto needs to be informed of the old information the first objeto knew.
     So:

     5. When the responsible objeto receives ObjetoReceiveSnapshot it now knows there is a new objeto and informs it of the old state by publishing to CotitularidadPublishSnapshot
     6. CotitularidadActor is listening to CotitularidadPublishSnapshot topic via the CotitularPublishSnapshotTransaction kafka consumer. It then publishes to the ObjetoReceiveSnapshot topic.

    This is the way objeto 1 updates objeto 2 of the old state, via publishing a snapshot which CotitularidadActor publishes
  
    Step by step: 
    --> ObjetoTri is sent to DGR-COP-OBJETOS-TRI
    --> ObjetoTributarioTransaction receives ObjetoTri and sends ObjetoUpdateFromTri
    --> ObjetoActor receives ObjetoUpdateFromTri and sends event ObjetoUpdatedFromTri tagged as 'ObjetoNovedadCotitularidad'
    --> ObjetoEventProcessor receives ObjetoUpdateFromTri and sends CotitularidadAddSujetoCotitular to topic 'AddCotitularTransaction'
    --> AddCotitularTransaction receives CotitularidadAddSujetoCotitular and sends CotitularidadAddSujetoCotitular to CotitularidadActor
    --> CotitularidadActor receives CotitularidadAddSujetoCotitular but because there is 2 cotitulares it does try to inform cotitulares of the new cotitular by sending an ObjetoSnapshot to 'ObjetoReceiveSnapshot' topic
    --> Both objetos, responsable and not responsable, first and second, receive this snapshot
        The responsible-objeto persists an ObjetoSnapshot tagged as 'ObjetoNovedadCotitularidad' to inform the new not-responsible objeto of the old information it may not know
    --> ObjetoEventProcessor receives ObjetoSnapshot and sends CotitularidadPublishSnapshot to topic 'CotitularPublishSnapshotTransaction'
    --> CotitularidadPublishSnapshot receives CotitularidadPublishSnapshot and sends an ObjetoSnapshot to the topic 'ObjetoReceiveSnapshot' for the not-responsible, new, second, objeto
    --> ObjetoUpdateNovedadTransaction receives ObjetoSnapshot and updates the second, newest objeto of the information the old, responsible objeto had
  """ in parallelActorSystemRunner { implicit s =>
    val context = testContext()

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
    context.close()
  }

  "asdThe third and fourth kafka messagess" should """
    be about the objetos informing they exist to the CotitularidadActor
      7. DGR-COP-OBLIGACIONES-TRI arrives
      8. The objeto 1, the first objeto, which is responsible, sends a tagged snapshot for all cotitulares to read
      9.CotitularidadActor, via CotitularidadPublishSnapshotTransaction takes this snapshot and publishes ObjetoReceiveSnapshot

    Step by step: 
    --> ObligacionesTri is sent to DGR-COP-OBLIGACIONES-TRI
    --> ObligacionTributariaTransaction receives ObligacionesTri and sends ObligacionUpdateFromDto to ObligacionActor
    --> ObligacionActor sends ObjetoUpdateFromObligacion to ObjetoActor
    --> ObjetoActor persists ObjetoPersistedSnapshot tagged as 'ObjetoNovedadCotitularidad'
    --> ObjetoEventProcessor receives ObjetoPersistedSnapshot and sends CotitularidadPublishSnapshot to topic 'CotitularidadPublishSnapshot'
    --> CotitularPublishSnapshotTransaction receive
    s CotitularidadPublishSnapshot and sends CotitularidadPublishSnapshot to CotitularidadActor
    --> CotitularidadActor receives CotitularidadPublishSnapshot and sends ObjetoSnapshot to the topic 'ObjetoReceiveSnapshot' for the not-responsible, new, second, objeto
    """ in parallelActorSystemRunner { implicit s =>
    val context = testContext()

    context.messageProcessor.messageHistory(8)._1 should be("DGR-COP-OBLIGACIONES-TRI")
    context.messageProcessor.messageHistory(9)._1 should be("CotitularidadPublishSnapshot")
    context.messageProcessor
      .messageHistory(10)
      ._1 should be("ObjetoReceiveSnapshot") // contains two messages one for each objeto which informs about the cotitulares Set(1,2)
    context.messageProcessor.messageHistory.size should be(11)
    context.close()
  }

}
