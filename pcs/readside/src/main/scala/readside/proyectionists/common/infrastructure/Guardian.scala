package readside.proyectionists.common.infrastructure

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.cluster.sharding.typed.scaladsl.ShardedDaemonProcess
import akka.cluster.sharding.typed.{ClusterShardingSettings, ShardedDaemonProcessSettings}
import akka.projection.ProjectionBehavior
import akka.projections.{ProjectionFactory, ProjectionSettings}
import org.slf4j.LoggerFactory
import readside.proyectionists.no_registrales.objeto.ObjetoProjectionHandler
import readside.proyectionists.no_registrales.obligacion.ObligacionProjectionHandler
import readside.proyectionists.no_registrales.sujeto.SujetoProjectionHandler
import readside.proyectionists.registrales.actividad_sujeto.ActividadSujetoProjectionHandler
import readside.proyectionists.registrales.declaracion_jurada.DeclaracionJuradaProjectionHandler
import readside.proyectionists.registrales.domicilio_objeto.DomicilioObjetoProjectionHandler
import readside.proyectionists.registrales.domicilio_sujeto.DomicilioSujetoProjectionHandler
import readside.proyectionists.registrales.etapas_procesales.EtapasProcesalesProjectionHandler
import readside.proyectionists.registrales.exencion.ExencionProjectionHandler
import readside.proyectionists.registrales.juicio.JuicioProjectionHandler
import readside.proyectionists.registrales.parametrica_plan.ParametricaPlanProjectionHandler
import readside.proyectionists.registrales.parametrica_recargo.ParametricaRecargoProjectionHandler
import readside.proyectionists.registrales.plan_pago.PlanPagoProjectionHandler
import readside.proyectionists.registrales.subasta.SubastaProjectionHandler
import readside.proyectionists.registrales.tramite.TramiteProjectionHandler

object Guardian {
  private val log = LoggerFactory.getLogger(this.getClass)
  def apply(): Behavior[Nothing] = {
    Behaviors.setup[Nothing] { context =>
      implicit val system: ActorSystem[Nothing] = context.system

      log.info("Running Proyections")
      val shardingSettings = ClusterShardingSettings(system)
      val shardedDaemonProcessSettings = ShardedDaemonProcessSettings(system).withShardingSettings(shardingSettings)

      val sujetoSettings = ProjectionSettings("Sujeto", 1)
      ShardedDaemonProcess(system).init(
        name = "SujetoProjection",
        sujetoSettings.parallelism,
        _ =>
          ProjectionBehavior(
            ProjectionFactory.createProjectionFor(
              system,
              "sujeto",
              new SujetoProjectionHandler(sujetoSettings, system)
            )
          ),
        shardedDaemonProcessSettings,
        Some(ProjectionBehavior.Stop)
      )

      val objetoSettings = ProjectionSettings("Objeto", 1)
      ShardedDaemonProcess(system).init(
        name = "ObjetoProjection",
        objetoSettings.parallelism,
        _ =>
          ProjectionBehavior(
            ProjectionFactory.createProjectionFor(
              system,
              "objeto",
              new ObjetoProjectionHandler(objetoSettings, system)
            )
          ),
        shardedDaemonProcessSettings,
        Some(ProjectionBehavior.Stop)
      )

      val obligacionSettings = ProjectionSettings("Obligacion", 1)
      ShardedDaemonProcess(system).init(
        name = "ObligacionProjection",
        obligacionSettings.parallelism,
        _ =>
          ProjectionBehavior(
            ProjectionFactory.createProjectionFor(
              system,
              "obligacion",
              new ObligacionProjectionHandler(obligacionSettings, system)
            )
          ),
        shardedDaemonProcessSettings,
        Some(ProjectionBehavior.Stop)
      )

      val actividadSujetoSettings = ProjectionSettings("ActividadSujeto", 1)
      ShardedDaemonProcess(system).init(
        name = "ActividadSujetoProjection",
        actividadSujetoSettings.parallelism,
        _ =>
          ProjectionBehavior(
            ProjectionFactory.createProjectionFor(
              system,
              "actividad-sujeto",
              new ActividadSujetoProjectionHandler(actividadSujetoSettings, system)
            )
          ),
        shardedDaemonProcessSettings,
        Some(ProjectionBehavior.Stop)
      )

      val declaracionJuradaSettings = ProjectionSettings("DeclaracionJurada", 1)
      ShardedDaemonProcess(system).init(
        name = "DeclaracionJuradaProjection",
        declaracionJuradaSettings.parallelism,
        _ =>
          ProjectionBehavior(
            ProjectionFactory.createProjectionFor(
              system,
              "declaracion-jurada",
              new DeclaracionJuradaProjectionHandler(declaracionJuradaSettings, system)
            )
          ),
        shardedDaemonProcessSettings,
        Some(ProjectionBehavior.Stop)
      )

      val domicilioObjetoSettings = ProjectionSettings("DomicilioObjeto", 1)
      ShardedDaemonProcess(system).init(
        name = "DomicilioObjetoProjection",
        domicilioObjetoSettings.parallelism,
        _ =>
          ProjectionBehavior(
            ProjectionFactory.createProjectionFor(
              system,
              "comicilio-objeto",
              new DomicilioObjetoProjectionHandler(domicilioObjetoSettings, system)
            )
          ),
        shardedDaemonProcessSettings,
        Some(ProjectionBehavior.Stop)
      )

      val domicilioSujetoSettings = ProjectionSettings("DomicilioSujeto", 1)
      ShardedDaemonProcess(system).init(
        name = "DomicilioSujetoProjection",
        domicilioSujetoSettings.parallelism,
        _ =>
          ProjectionBehavior(
            ProjectionFactory.createProjectionFor(
              system,
              "domicilio-sujeto",
              new DomicilioSujetoProjectionHandler(domicilioSujetoSettings, system)
            )
          ),
        shardedDaemonProcessSettings,
        Some(ProjectionBehavior.Stop)
      )

      val etapasProcesalesSettings = ProjectionSettings("EtapasProcesales", 1)
      ShardedDaemonProcess(system).init(
        name = "EtapasProcesalesProjection",
        etapasProcesalesSettings.parallelism,
        _ =>
          ProjectionBehavior(
            ProjectionFactory.createProjectionFor(
              system,
              "etapas-procesales",
              new EtapasProcesalesProjectionHandler(etapasProcesalesSettings, system)
            )
          ),
        shardedDaemonProcessSettings,
        Some(ProjectionBehavior.Stop)
      )

      val exencionSettings = ProjectionSettings("Exencion", 1)
      ShardedDaemonProcess(system).init(
        name = "ExencionProjection",
        exencionSettings.parallelism,
        _ =>
          ProjectionBehavior(
            ProjectionFactory.createProjectionFor(
              system,
              "exencion",
              new ExencionProjectionHandler(exencionSettings, system)
            )
          ),
        shardedDaemonProcessSettings,
        Some(ProjectionBehavior.Stop)
      )

      val juicioSettings = ProjectionSettings("Juicio", 1)
      ShardedDaemonProcess(system).init(
        name = "JuicioProjection",
        juicioSettings.parallelism,
        _ =>
          ProjectionBehavior(
            ProjectionFactory.createProjectionFor(
              system,
              "juicio",
              new JuicioProjectionHandler(juicioSettings, system)
            )
          ),
        shardedDaemonProcessSettings,
        Some(ProjectionBehavior.Stop)
      )

      val parametricaPlanSettings = ProjectionSettings("ParametricaPlan", 1)
      ShardedDaemonProcess(system).init(
        name = "ParametricaPlanProjection",
        parametricaPlanSettings.parallelism,
        _ =>
          ProjectionBehavior(
            ProjectionFactory.createProjectionFor(
              system,
              "parametrica-plan",
              new ParametricaPlanProjectionHandler(parametricaPlanSettings, system)
            )
          ),
        shardedDaemonProcessSettings,
        Some(ProjectionBehavior.Stop)
      )

      val parametricaRecargoSettings = ProjectionSettings("ParametricaRecargo", 1)
      ShardedDaemonProcess(system).init(
        name = "ParametricaRecargoProjection",
        parametricaRecargoSettings.parallelism,
        _ =>
          ProjectionBehavior(
            ProjectionFactory.createProjectionFor(
              system,
              "parametrica-recargo",
              new ParametricaRecargoProjectionHandler(parametricaRecargoSettings, system)
            )
          ),
        shardedDaemonProcessSettings,
        Some(ProjectionBehavior.Stop)
      )

      val planPagoSettings = ProjectionSettings("PlanPago", 1)
      ShardedDaemonProcess(system).init(
        name = "PlanPagoProjection",
        planPagoSettings.parallelism,
        _ =>
          ProjectionBehavior(
            ProjectionFactory.createProjectionFor(
              system,
              "plan-pago",
              new PlanPagoProjectionHandler(planPagoSettings, system)
            )
          ),
        shardedDaemonProcessSettings,
        Some(ProjectionBehavior.Stop)
      )

      val subastaSettings = ProjectionSettings("Subasta", 1)
      ShardedDaemonProcess(system).init(
        name = "SubastaProjection",
        subastaSettings.parallelism,
        _ =>
          ProjectionBehavior(
            ProjectionFactory.createProjectionFor(
              system,
              "subasta",
              new SubastaProjectionHandler(subastaSettings, system)
            )
          ),
        shardedDaemonProcessSettings,
        Some(ProjectionBehavior.Stop)
      )
      val tramiteSettings = ProjectionSettings("Tramite", 1)
      ShardedDaemonProcess(system).init(
        name = "TramiteProjection",
        tramiteSettings.parallelism,
        _ =>
          ProjectionBehavior(
            ProjectionFactory.createProjectionFor(
              system,
              "tramite",
              new TramiteProjectionHandler(tramiteSettings, system)
            )
          ),
        shardedDaemonProcessSettings,
        Some(ProjectionBehavior.Stop)
      )

      Behaviors.empty
    }
  }
}
