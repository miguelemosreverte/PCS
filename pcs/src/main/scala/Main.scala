import java.util.concurrent.{ExecutorService, Executors, ForkJoinPool}

import com.typesafe.config.ConfigFactory
import design_principles.microservice.Microservice
import design_principles.microservice.kafka_consumer_microservice.KafkaConsumerMicroservice
import design_principles.microservice.kafka_consumer_microservice.MainApplication.startMicroservices

object Main extends App {

  val config = ConfigFactory.load()
  val ip = config.getString("http.ip")
  val port = config.getInt("http.port")
  val actorSystemName = "PersonClassificationService"

  val cores = 8
  val pool: ExecutorService = Executors.newFixedThreadPool(cores)

  val forkJoinPool = new ForkJoinPool(2)

  startMicroservices(microservices, ip, port, actorSystemName)

  def microservices: Seq[KafkaConsumerMicroservice] = Seq(
    consumers.no_registral.sujeto.infrastructure.main.SujetoMicroservice,
    consumers.no_registral.cotitularidad.infrastructure.main.CotitularidadMicroservice,
    consumers.no_registral.objeto.infrastructure.main.ObjetoMicroservice,
    consumers.no_registral.obligacion.infrastructure.main.ObligacionMicroservice,
    consumers.registral.actividad_sujeto.infrastructure.main.ActividadSujetoMicroservice,
    consumers.registral.calendario.infrastructure.main.CalendarioMicroservice,
    //consumers.registral.contacto.infrastructure.main.ContactoMicroservice,
    consumers.registral.declaracion_jurada.infrastructure.main.DeclaracionJuradaMicroservice,
    consumers.registral.domicilio_objeto.infrastructure.main.DomicilioObjetoMicroservice,
    consumers.registral.domicilio_sujeto.infrastructure.main.DomicilioSujetoMicroservice,
    consumers.registral.etapas_procesales.infrastructure.main.EtapasProcesalesMicroservice,
    consumers.registral.juicio.infrastructure.main.JuicioMicroservice,
    consumers.registral.parametrica_plan.infrastructure.main.ParametricaPlanMicroservice,
    consumers.registral.parametrica_recargo.infrastructure.main.ParametricaRecargoMicroservice,
    consumers.registral.plan_pago.infrastructure.main.PlanPagoMicroservice,
    consumers.registral.subasta.infrastructure.main.SubastaMicroservice,
    consumers.registral.tramite.infrastructure.main.TramiteMicroservice
  )
}
