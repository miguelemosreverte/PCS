package consumers_spec.no_registrales.obligacion

import consumers_spec.no_registrales.testkit.Examples
import consumers_spec.no_registrales.testsuite.NoRegistralesTestSuite

trait ObligacionSpec extends NoRegistralesTestSuite {
  type AggregateRoot = String
  val examples = new Examples("ObligacionSpec")

  "una obligacion" should
  "pisar una obligacion con otra nueva que llegue desde Kafka para el mismo ID" in parallelActorSystemRunner {
    implicit s =>
      val context = testContext()

      context.messageProducer produceObligacion examples.obligacionWithSaldo200
      eventually {
        val response = context.Query getStateObligacion examples.obligacionWithSaldo200
        response.saldo should be(examples.obligacionWithSaldo200.BOB_SALDO)
        response.vencida should be(false)

      }

      context.messageProducer produceObligacion examples.obligacionWithSaldo50
      eventually {
        val response = context.Query getStateObligacion examples.obligacionWithSaldo50
        response.saldo should be(examples.obligacionWithSaldo50.BOB_SALDO)
        response.vencida should be(false)
      }
      context.close()
  }

  "una obligacion" should "volverse vencida si se recibe un movimiento luego de la fecha de expiracion esperada" in
  parallelActorSystemRunner { implicit s =>
    val context = testContext()

    context.messageProducer produceObligacion examples.obligacionVencida
    eventually {
      val response = context.Query getStateObligacion examples.obligacionVencida
      response.vencida should be(true)
    }
    context.close()
  }

  "una obligacion" should "mostrar el id de juicio al recibir BOB_JUI_ID no nulo en el documento" in parallelActorSystemRunner {
    implicit s =>
      val context = testContext()
      context.messageProducer produceObligacion examples.obligacionWithJuicio
      eventually {
        val response = context.Query getStateObligacion examples.obligacionWithJuicio
        response.juicioId should contain(examples.juicioId)
      }
      context.close()
  }

  "una obligacion" should
  "eliminar una obligacion si llega otra nueva que llegue desde Kafka para el mismo ID y con el atributo estado con BAJA" in parallelActorSystemRunner {
    implicit s =>
      val context = testContext()
      context.messageProducer produceObligacion examples.obligacionWithSaldo200
      eventually {
        val response = context.Query getStateObligacion examples.obligacionWithSaldo200
        response.saldo should be(examples.obligacionWithSaldo200.BOB_SALDO)
      }

      context.messageProducer produceObligacion examples.obligacionWithSaldo200.copy(
        BOB_ESTADO = Some("BAJA")
      )
      eventually {
        val response = context.Query getStateObligacion examples.obligacionWithSaldo200
        response.saldo should be(0)
      }
  }

}
