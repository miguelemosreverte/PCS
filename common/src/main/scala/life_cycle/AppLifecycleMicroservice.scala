package life_cycle

import akka.actor.{typed, ActorSystem}
import akka.http.scaladsl.server.Route
import design_principles.microservice.{Microservice, MicroserviceRequirements}
import life_cycle.typed.controller.{LivenessController, ReadinessController, ShutdownController}
import life_cycle.typed.{AppLifecycle, AppLifecycleActor}
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContext

object AppLifecycleMicroservice extends Microservice {
  def route(microserviceRequirements: MicroserviceRequirements): Route = {
    val ctx = microserviceRequirements.ctx
    implicit val system: typed.ActorSystem[Nothing] = ctx.system
    implicit val systemClassic: ActorSystem = ctx.system.toClassic
    implicit val ec: ExecutionContext = microserviceRequirements.executionContext
    val monitoring = microserviceRequirements.monitoring

    val appLifecycle = new AppLifecycle(AppLifecycleActor.init(ctx.system))
    val livenessController = new LivenessController(monitoring)
    val readinessController = new ReadinessController(appLifecycle, monitoring)
    val shutdownController = new ShutdownController(appLifecycle, monitoring)

    val appLifeCycleRoutes =
      livenessController.route ~
      readinessController.route ~
      shutdownController.route
    appLifeCycleRoutes
  }
}
