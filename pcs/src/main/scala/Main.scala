import akka.Done
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.ActorContext
import akka.cluster.ClusterEvent.MemberUp
import akka.http.AkkaHttpServer
import akka.http.scaladsl.server.Route
import akka.kafka.ConsumerRebalanceEvent
import com.typesafe.config.ConfigFactory
import consumers.no_registral.cotitularidad.infrastructure.main.CotitularidadMicroservice
import consumers.no_registral.objeto.infrastructure.main.ObjetoMicroservice
import consumers.no_registral.obligacion.infrastructure.main.ObligacionMicroservice
import consumers.no_registral.sujeto.infrastructure.main.{MicroserviceRequirements, SujetoMicroservice}
import consumers.registral.actividad_sujeto.infrastructure.main.ActividadSujetoMicroservice
import consumers.registral.calendario.infrastructure.main.CalendarioMicroservice
import consumers.registral.declaracion_jurada.infrastructure.main.DeclaracionJuradaMicroservice
import consumers.registral.domicilio_objeto.infrastructure.main.DomicilioObjetoMicroservice
import consumers.registral.domicilio_sujeto.infrastructure.main.DomicilioSujetoMicroservice
import consumers.registral.etapas_procesales.infrastructure.main.EtapasProcesalesMicroservice
import consumers.registral.juicio.infrastructure.main.JuicioMicroservice
import consumers.registral.parametrica_plan.infrastructure.main.ParametricaPlanMicroservice
import consumers.registral.parametrica_recargo.infrastructure.main.ParametricaRecargoMicroservice
import consumers.registral.plan_pago.infrastructure.main.PlanPagoMicroservice
import consumers.registral.subasta.infrastructure.main.SubastaMicroservice
import consumers.registral.tramite.infrastructure.main.TramiteMicroservice
import kafka.{KafkaMessageProcessorRequirements, TopicListener}
import life_cycle.controller.LivenessController
import life_cycle.typed.controller.{ReadinessControllerTyped, ShutdownControllerTyped}
import life_cycle.typed.{AppLifecycleActorTyped, AppLifecycleTyped}
import monitoring.KamonMonitoring
import serialization.EventSerializer

object Main extends App {

  lazy val config = Seq(
    ConfigFactory.load(),
    ConfigFactory parseString EventSerializer.eventAdapterConf,
    ConfigFactory parseString EventSerializer.serializationConf
  ).reduce(_ withFallback _)

  val ip = config.getString("http.ip")
  val port = config.getInt("http.port")

  val actorSystemName = "PersonClassificationService"

  val systemSetup = new AkkaSystemFactory(actorSystemName, config)
  import ProductionMicroserviceContextProvider.start
  systemSetup isReady {
    start(microservices)
  }

  object ProductionMicroserviceContextProvider {

    def getContext(ctx: ActorContext[MemberUp]): MicroserviceRequirements = {
      import akka.actor.typed.scaladsl.adapter._

      val monitoring = new KamonMonitoring

      val rebalancerListener: ActorRef[ConsumerRebalanceEvent] =
        ctx.spawn(TopicListener("rebalancerListener"), "rebalancerListener")

      implicit val s = ctx.system.toClassic
      val transactionRequirements: KafkaMessageProcessorRequirements =
        KafkaMessageProcessorRequirements.productionSettings(Some(rebalancerListener.toClassic), monitoring)

      MicroserviceRequirements(
        monitoring = monitoring,
        executionContext = ctx.system.toClassic.dispatcher,
        system = ctx.system.toClassic,
        kafkaMessageProcessorRequirements = transactionRequirements
      )
    }

    import akka.http.scaladsl.server.Directives._

    def systemMicroservices(microserviceRequirements: MicroserviceRequirements, ctx: ActorContext[MemberUp]): Route = {
      implicit val system = microserviceRequirements.system
      implicit val systemC = ctx.system
      implicit val ec = microserviceRequirements.executionContext
      val monitoring = microserviceRequirements.monitoring
      val appLifecycle = new AppLifecycleTyped(AppLifecycleActorTyped.init(ctx.system))
      val livenessController = new LivenessController(monitoring)
      val readinessController = new ReadinessControllerTyped(appLifecycle, monitoring)
      val shutdownController = new ShutdownControllerTyped(appLifecycle, monitoring)
      val appLifeCycleRoutes =
        livenessController.route ~
        readinessController.route ~
        shutdownController.route
      appLifeCycleRoutes

    }

    def start(
        userMicroservices: Seq[MicroserviceRequirements => Route]
    )(ctx: ActorContext[MemberUp]): Behavior[Done] = {
      val microserviceRequirements = getContext(ctx)

      val userRoutes = userMicroservices.map(_(microserviceRequirements)).reduce(_ ~ _)
      val systemRoutes = systemMicroservices(microserviceRequirements, ctx)

      val route = userRoutes ~ systemRoutes

      AkkaHttpServer.start(route, ip, port)(ctx)
    }

  }
  def microservices: Seq[MicroserviceRequirements => Route] = Seq(
    SujetoMicroservice route,
    CotitularidadMicroservice route,
    ObjetoMicroservice route,
    ObligacionMicroservice route,
    ActividadSujetoMicroservice route,
    CalendarioMicroservice route,
    //ContactoMicroservice route,
    DeclaracionJuradaMicroservice route,
    DomicilioObjetoMicroservice route,
    DomicilioSujetoMicroservice route,
    EtapasProcesalesMicroservice route,
    JuicioMicroservice route,
    ParametricaPlanMicroservice route,
    ParametricaRecargoMicroservice route,
    PlanPagoMicroservice route,
    SubastaMicroservice route,
    TramiteMicroservice route
  )

}
