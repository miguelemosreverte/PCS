package consumers_spec.no_registrales.sujeto

import consumers_spec.no_registrales.testkit.Examples
import consumers_spec.no_registrales.testsuite.NoRegistralesTestSuite
import utils.generators.Model.deliveryId

trait SujetoSpec extends NoRegistralesTestSuite {
  val examples = new Examples("SujetoSpec")

  "un sujeto" should
  "pisar una obligacion con otra nueva que llegue desde Kafka para el mismo ID" in parallelActorSystemRunner {
    implicit s =>
      val context = testContext()

      context.messageProducer produceObligacion examples.obligacionWithSaldo200
      eventually {
        val response = context.Query getStateSujeto examples.obligacionWithSaldo200
        response.saldo should be(examples.obligacionWithSaldo200.BOB_SALDO)
      }

      context.messageProducer produceObligacion examples.obligacionWithSaldo50
      eventually {
        val response = context.Query getStateSujeto examples.obligacionWithSaldo50
        response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO)
      }
      context.close()
  }

  "un sujeto" should
  "acumular saldo para diferentes objetos" in parallelActorSystemRunner { implicit s =>
    val context = testContext()

    val anotherOne = examples.obligacionWithSaldo50.copy(
      BOB_SOJ_IDENTIFICADOR = "anotherObject",
      BOB_OBN_ID = "anotherObligation",
      EV_ID = deliveryId
    )
    context.messageProducer produceObligacion examples.obligacionWithSaldo50
    context.messageProducer produceObligacion anotherOne
    eventually {
      val response = context.Query getStateSujeto examples.obligacionWithSaldo50
      response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO + anotherOne.BOB_SALDO)
    }
    context.close()
  }

  "un sujeto" should
  "Desacumular saldo a partir de la baja de un objeto" in parallelActorSystemRunner { implicit s =>
    val context = testContext()

    val anotherOne = examples.obligacionWithSaldo50.copy(
      BOB_SOJ_IDENTIFICADOR = "anotherObject",
      BOB_OBN_ID = "anotherObligation",
      EV_ID = deliveryId
    )
    context.messageProducer produceObligacion examples.obligacionWithSaldo50
    context.messageProducer produceObligacion anotherOne
    eventually {
      val response = context.Query getStateSujeto examples.obligacionWithSaldo50
      response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO + anotherOne.BOB_SALDO)
    }
    val anotherOneBaja = examples.objeto2.copy(
      SOJ_IDENTIFICADOR = "anotherObject",
      SOJ_ESTADO = Some("BAJA")
    )
    context.messageProducer produceObjeto anotherOneBaja
    eventually {
      val response = context.Query getStateSujeto examples.obligacionWithSaldo50
      response.objetos.size should be(1)
      response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO)
    }
    context.close()
  }
}
