package consumers_spec.no_registrales.objeto

import consumers_spec.no_registrales.testkit.Examples
import consumers_spec.no_registrales.testsuite.NoRegistralesTestSuite
import utils.generators.Model.deliveryId
import consumers_spec.Utils.isObjetoBajaFromGetObjetoResponse

trait ObjetoSpec extends NoRegistralesTestSuite {
  val examples = new Examples("ObjetoSpec")

  "un objeto" should
  "pisar una obligacion con otra nueva que llegue desde Kafka para el mismo ID" in parallelActorSystemRunner {
    implicit s =>
      val context = testContext()

      context.messageProducer produceObligacion examples.obligacionWithSaldo200
      eventually {
        val response = context.Query getStateObjeto examples.obligacionWithSaldo200
        response.saldo should be(examples.obligacionWithSaldo200.BOB_SALDO)
        response.vencimiento should be(false)
      }

      context.messageProducer produceObligacion examples.obligacionWithSaldo50
      eventually {
        val response = context.Query getStateObjeto examples.obligacionWithSaldo50
        response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO)
        response.vencimiento should be(false)
      }
      context.close()
  }

  "un objeto" should
  "acumular saldo para diferentes obligaciones" in parallelActorSystemRunner { implicit s =>
    val context = testContext()

    val anotherOne = examples.obligacionWithSaldo50.copy(
      BOB_OBN_ID = "anotherObligation",
      EV_ID = deliveryId
    )
    context.messageProducer produceObligacion examples.obligacionWithSaldo50
    context.messageProducer produceObligacion anotherOne
    eventually {
      val response = context.Query getStateObjeto examples.obligacionWithSaldo50
      response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO + anotherOne.BOB_SALDO)
    }
    context.close()
  }

  "un objeto" should
  "declararse vencido si una de sus obligaciones se declara vencida" in parallelActorSystemRunner { implicit s =>
    val context = testContext()

    context.messageProducer produceObligacion examples.obligacionVencida
    eventually {
      val response = context.Query getStateObjeto examples.obligacionVencida
      response.vencimiento should be(true)
    }
    context.close()
  }

  "un objeto" should
  "trasladar el estado baja a su state" in parallelActorSystemRunner { implicit s =>
    val context = testContext()

    context.messageProducer produceObjeto examples.objeto2
    eventually {
      val response = context.Query getStateObjeto examples.obligacionWithSaldo50
      val isBaja: Boolean = isObjetoBajaFromGetObjetoResponse(response)
      isBaja should be(false)
    }
    context.messageProducer produceObjeto examples.objeto2.copy(
      SOJ_ESTADO = Some("BAJA")
    )
    eventually {
      val response = context.Query getStateObjeto examples.obligacionWithSaldo50
      val isBaja: Boolean = isObjetoBajaFromGetObjetoResponse(response)
      isBaja should be(true)
    }
    context.close()
  }
}
