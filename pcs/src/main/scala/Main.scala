import akka.actor.ActorSystem
import akka.entity.ShardedEntity.NoRequirements
import akka.http.AkkaHttpServer
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

  val log = LoggerFactory.getLogger(this.getClass)

  lazy val config = Seq(
    ConfigFactory.load(),
    ConfigFactory parseString EventSerializer.eventAdapterConf,
    ConfigFactory parseString EventSerializer.serializationConf
  ).reduce(_ withFallback _)

  implicit val system: ActorSystem = ActorSystem("PersonClassificationService", config)

  AkkaManagement(system).start()
  ClusterBootstrap(system).start()

  val route: Route = Seq(
      consumers.no_registral.sujeto.infrastructure.main.SujetoMicroservice.route(monitoring),
      consumers.no_registral.cotitularidad.infrastructure.main.CotitularidadMicroservice.route(monitoring),
      consumers.no_registral.objeto.infrastructure.main.ObjetoMicroservice.route(monitoring),
      consumers.no_registral.obligacion.infrastructure.main.ObligacionMicroservice.route(monitoring),
      consumers.registral.actividad_sujeto.infrastructure.main.ActividadSujetoMicroservice.route(monitoring),
      consumers.registral.calendario.infrastructure.main.CalendarioMicroservice.route(monitoring),
      //consumers.registral.contacto.application.main.ContactoMicroservice.route(monitoring),
      consumers.registral.declaracion_jurada.infrastructure.main.DeclaracionJuradaMicroservice.route(monitoring),
      consumers.registral.domicilio_objeto.infrastructure.main.DomicilioObjetoMicroservice.route(monitoring),
      consumers.registral.domicilio_sujeto.infrastructure.main.DomicilioSujetoMicroservice.route(monitoring),
      consumers.registral.etapas_procesales.infrastructure.main.EtapasProcesalesMicroservice.route(monitoring),
      consumers.registral.juicio.infrastructure.main.JuicioMicroservice.route(monitoring),
      consumers.registral.parametrica_plan.infrastructure.main.ParametricaPlanMicroservice.route(monitoring),
      consumers.registral.parametrica_recargo.infrastructure.main.ParametricaRecargoMicroservice.route(monitoring),
      consumers.registral.plan_pago.infrastructure.main.PlanPagoMicroservice.route(monitoring),
      consumers.registral.subasta.infrastructure.main.SubastaMicroservice.route(monitoring),
      consumers.registral.tramite.infrastructure.main.TramiteMicroservice.route(monitoring)
    ) reduce (_ ~ _)

  log.info("Running Feedback Loop") // Notificador
  import akka.actor.typed.scaladsl.adapter._
  system.spawn[Nothing](Guardian.apply(), "ObjetoNovedadCotitularidad")

  private val appLifecycle = new AppLifecycle(AppLifecycleActor.startWithRequirements(NoRequirements()))
  implicit val ec: ExecutionContext = system.dispatcher
  private val livenessController = new LivenessController(monitoring)
  private val readinessController = new ReadinessController(appLifecycle, monitoring)
  private val shutdownController = new ShutdownController(appLifecycle, monitoring)
  private val appLifeCycleRoutes =
    livenessController.route ~
    readinessController.route ~
    shutdownController.route

  AkkaHttpServer.start(route ~ appLifeCycleRoutes)
  Await.result(system.whenTerminated, Duration.Inf)

}
