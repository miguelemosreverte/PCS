package readside.proyectionists.common.infrastructure

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.{actor => classic}
import life_cycle.controller.LivenessController
import life_cycle.typed.controller.{ReadinessControllerTyped, ShutdownControllerTyped}
import life_cycle.typed.{AppLifecycleActorTyped, AppLifecycleTyped}
import monitoring.Monitoring

import scala.concurrent.ExecutionContext

class ReadSideHttpRoutes(monitoring: Monitoring)(implicit system: ActorSystem[_]) {
  import akka.actor.typed.scaladsl.adapter._
  implicit val classicSystem: classic.ActorSystem = system.toClassic

  implicit val ec: ExecutionContext = classicSystem.dispatcher

  private val appLifecycle = new AppLifecycleTyped(AppLifecycleActorTyped.init(system))
  private val livenessController = new LivenessController(monitoring)
  private val readinessController = new ReadinessControllerTyped(appLifecycle, monitoring)
  private val shutdownController = new ShutdownControllerTyped(appLifecycle, monitoring)
  private val appLifeCycleRoutes =
    livenessController.route ~
    readinessController.route ~
    shutdownController.route

  val readSide: Route = appLifeCycleRoutes
}
