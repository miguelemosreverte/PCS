package life_cycle

import akka.actor.{Actor, ActorLogging, Props}
import akka.entity.ShardedEntity.NoRequirements
import akka.entity.SingletonEntity

import scala.concurrent.duration._

object AppLifecycleActor extends SingletonEntity[NoRequirements] {
  def props(requirements: NoRequirements): Props =
    Props(new AppLifecycleActor)

  case object Shutdown
  case object IsAppReady
  case object ShutdownModuleRegistry
}

class AppLifecycleActor extends Actor with ActorLogging {

  val delayStopModuleRegistry = 40.seconds

  import AppLifecycleActor._
  implicit val ec = context.system.dispatcher

  var state = AppLifecycleActorState()

  override def receive: Receive = {
    case Shutdown =>
      if (!state.isAppShuttingDown) {
        state = state.shutdown()
        context.system.scheduler.scheduleOnce(delayStopModuleRegistry, self, ShutdownModuleRegistry)
      }
    case IsAppReady => sender() ! state.isReady

    case ShutdownModuleRegistry =>
      // @TODO shut down module registry
      log.warning("Module registry shutted down gracefully")
  }
}
