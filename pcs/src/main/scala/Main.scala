import akka.AkkaHttpServer
import akka.actor.ActorSystem
import akka.entity.ShardedEntity.NoRequirements
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import monitoring.KamonMonitoring
import com.typesafe.config.ConfigFactory
import consumers.no_registral.objeto.infrastructure.event_processor.Guardian
import kamon.Kamon
import kamon.prometheus.PrometheusReporter
import life_cycle.controller.LivenessController
import life_cycle.untyped.controller.{ReadinessController, ShutdownController}
import life_cycle.untyped.{AppLifecycle, AppLifecycleActor}
import org.slf4j.LoggerFactory
import serialization.EventSerializer

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

object Main extends App {
  val monitoring = new KamonMonitoring

  val pepeCounter = monitoring.counter("pepe")
  val pepeHistogram = monitoring.histogram("pepe")
  val pepeGauge = monitoring.gauge("pepe")
  // One-liner
  (1 to 1000) foreach { i =>
    pepeCounter.increment()
    pepeGauge.increment()
    pepeHistogram.record(i)

  }

  val log = LoggerFactory.getLogger(this.getClass)

  lazy val config = Seq(
    ConfigFactory.load(),
    ConfigFactory parseString EventSerializer.eventAdapterConf,
    ConfigFactory parseString EventSerializer.serializationConf
  ).reduce(_ withFallback _)

  implicit val system: ActorSystem = ActorSystem("PersonClassificationService", config)

  AkkaManagement(system).start()
  ClusterBootstrap(system).start()

  val routes: Route = Seq(
      consumers.no_registral.sujeto.infrastructure.main.SujetoMicroservice.routes,
      consumers.no_registral.cotitularidad.infrastructure.main.CotitularidadMicroservice.routes,
      consumers.no_registral.objeto.infrastructure.main.ObjetoMicroservice.routes,
      consumers.no_registral.obligacion.infrastructure.main.ObligacionMicroservice.routes,
      consumers.registral.actividad_sujeto.infrastructure.main.ActividadSujetoMicroservice.routes,
      consumers.registral.calendario.infrastructure.main.CalendarioMicroservice.routes,
      //consumers.registral.contacto.application.main.ContactoMicroservice.routes,
      consumers.registral.declaracion_jurada.infrastructure.main.DeclaracionJuradaMicroservice.routes,
      consumers.registral.domicilio_objeto.infrastructure.main.DomicilioObjetoMicroservice.routes,
      consumers.registral.domicilio_sujeto.infrastructure.main.DomicilioSujetoMicroservice.routes,
      consumers.registral.etapas_procesales.infrastructure.main.EtapasProcesalesMicroservice.routes,
      consumers.registral.juicio.infrastructure.main.JuicioMicroservice.routes,
      consumers.registral.parametrica_plan.infrastructure.main.ParametricaPlanMicroservice.routes,
      consumers.registral.parametrica_recargo.infrastructure.main.ParametricaRecargoMicroservice.routes,
      consumers.registral.plan_pago.infrastructure.main.PlanPagoMicroservice.routes,
      consumers.registral.subasta.infrastructure.main.SubastaMicroservice.routes,
      consumers.registral.tramite.infrastructure.main.TramiteMicroservice.routes
    ) reduce (_ ~ _)

  log.info("Running Feedback Loop") // Notificador
  import akka.actor.typed.scaladsl.adapter._
  system.spawn[Nothing](Guardian.apply(), "ObjetoNovedadCotitularidad")

  private val appLifecycle = new AppLifecycle(AppLifecycleActor.startWithRequirements(NoRequirements()))
  implicit val ec: ExecutionContext = system.dispatcher
  private val livenessController = new LivenessController
  private val readinessController = new ReadinessController(appLifecycle)
  private val shutdownController = new ShutdownController(appLifecycle)
  private val appLifeCycleRoutes =
    livenessController.route ~
    readinessController.route ~
    shutdownController.route

  AkkaHttpServer.start(routes ~ appLifeCycleRoutes)
  Await.result(system.whenTerminated, Duration.Inf)

}
