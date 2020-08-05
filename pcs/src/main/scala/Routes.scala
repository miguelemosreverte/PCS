import akka.Done
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.http.AkkaHttpServer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import consumers.no_registral.objeto.infrastructure.event_processor.Guardian
import kafka.KafkaMessageProcessorRequirements
import monitoring.Monitoring

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Routes extends App {

  def apply(monitoring: Monitoring)(
      implicit
      system: akka.actor.ActorSystem,
      ec: ExecutionContext,
      kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements
  ): Behavior[Done] =
    Behaviors.setup[Done] { ctx =>
      val route: Route = Seq(
          consumers.no_registral.sujeto.infrastructure.main.SujetoMicroservice
            .route(monitoring, ec),
          consumers.no_registral.cotitularidad.infrastructure.main.CotitularidadMicroservice
            .route(monitoring, ec),
          consumers.no_registral.objeto.infrastructure.main.ObjetoMicroservice
            .route(monitoring, ec),
          consumers.no_registral.obligacion.infrastructure.main.ObligacionMicroservice
            .route(monitoring, ec),
          consumers.registral.actividad_sujeto.infrastructure.main.ActividadSujetoMicroservice
            .route(monitoring, ec),
          consumers.registral.calendario.infrastructure.main.CalendarioMicroservice
            .route(monitoring, ec),
          //consumers.registral.contacto.application.main.ContactoMicroservice.route(monitoring),
          consumers.registral.declaracion_jurada.infrastructure.main.DeclaracionJuradaMicroservice
            .route(monitoring, ec),
          consumers.registral.domicilio_objeto.infrastructure.main.DomicilioObjetoMicroservice
            .route(monitoring, ec),
          consumers.registral.domicilio_sujeto.infrastructure.main.DomicilioSujetoMicroservice
            .route(monitoring, ec),
          consumers.registral.etapas_procesales.infrastructure.main.EtapasProcesalesMicroservice
            .route(monitoring, ec),
          consumers.registral.juicio.infrastructure.main.JuicioMicroservice
            .route(monitoring, ec),
          consumers.registral.parametrica_plan.infrastructure.main.ParametricaPlanMicroservice
            .route(monitoring, ec),
          consumers.registral.parametrica_recargo.infrastructure.main.ParametricaRecargoMicroservice
            .route(monitoring, ec),
          consumers.registral.plan_pago.infrastructure.main.PlanPagoMicroservice
            .route(monitoring, ec),
          consumers.registral.subasta.infrastructure.main.SubastaMicroservice
            .route(monitoring, ec),
          consumers.registral.tramite.infrastructure.main.TramiteMicroservice
            .route(monitoring, ec)
        ) reduce (_ ~ _)
      ctx.spawn[Nothing](Guardian.apply(), "ObjetoNovedadCotitularidad")

      /* val appLifecycle = new AppLifecycle(AppLifecycleActor.startWithRequirements(NoRequirements()))
      val livenessController = new LivenessController(monitoring)
      val readinessController = new ReadinessController(appLifecycle, monitoring)
      val shutdownController = new ShutdownController(appLifecycle, monitoring)
      val appLifeCycleRoutes =
        livenessController.route ~
        readinessController.route ~
        shutdownController.route*/

      val serverBinding = AkkaHttpServer.start(route) // ~ appLifeCycleRoutes)
      serverBinding.onComplete {
        case Success(bound) =>
          println(
            s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/"
          )
        case Failure(e) =>
          Console.err.println(s"Server could not start!")
          e.printStackTrace()
          ctx.self ! Done
      }

      Behaviors.receiveMessage[Done] { _ =>
        ctx.log.info("Consumer stopped {}")
        Behaviors.stopped
      }

    }

}
