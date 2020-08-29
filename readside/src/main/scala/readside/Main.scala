package readside

import com.typesafe.config.{Config, ConfigFactory}
import design_principles.microservice.cassandra_projectionist_microservice.CassandraProjectionistMicroservice
import design_principles.microservice.cassandra_projectionist_microservice.MainApplication.startMicroservices
import monitoring.KamonMonitoring

object Main extends App {

  private val config: Config = ConfigFactory.load()
  val ip = config.getString("http.ip")
  val port = config.getInt("http.port")
  val actorSystemName = "PersonClassificationServiceReadSide"

  startMicroservices(microservices, ip, port, actorSystemName)

  def microservices: Seq[CassandraProjectionistMicroservice] = Seq(
    readside.proyectionists.no_registrales.obligacion.infrastructure.main.ObligacionProjectionistMicroservice,
    readside.proyectionists.no_registrales.objeto.infrastructure.main.ObjetoProjectionistMicroservice,
    readside.proyectionists.no_registrales.sujeto.infrastructure.main.SujetoProjectionistMicroservice,
    readside.proyectionists.registrales.actividad_sujeto.infrastructure.main.ActividadSujetoProjectionistMicroservice,
    readside.proyectionists.registrales.declaracion_jurada.infrastructure.main.DeclaracionJuradaProjectionistMicroservice,
    readside.proyectionists.registrales.domicilio_objeto.infrastructure.main.DomicilioObjetoProjectionistMicroservice,
    readside.proyectionists.registrales.domicilio_sujeto.infrastructure.main.DomicilioSujetoProjectionistMicroservice,
    readside.proyectionists.registrales.etapas_procesales.infrastructure.main.EtapasProcesalesProjectionistMicroservice,
    readside.proyectionists.registrales.exencion.infrastructure.main.ExencionProjectionistMicroservice,
    readside.proyectionists.registrales.juicio.infrastructure.main.JuicioProjectionistMicroservice,
    readside.proyectionists.registrales.parametrica_plan.infrastructure.main.ParametricaPlanProjectionistMicroservice,
    readside.proyectionists.registrales.parametrica_recargo.infrastructure.main.ParametricaRecargoProjectionistMicroservice,
    readside.proyectionists.registrales.plan_pago.infrastructure.main.PlanPagoProjectionistMicroservice,
    readside.proyectionists.registrales.subasta.infrastructure.main.SubastaProjectionistMicroservice,
    readside.proyectionists.registrales.tramite.infrastructure.main.TramiteProjectionistMicroservice
  )
}
