package life_cycle

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import life_cycle.AppLifecycleActor.{IsAppReady, Shutdown}

import scala.concurrent.Future
import scala.concurrent.duration._

class AppLifecycle(actor: ActorRef) {
  implicit private val akkaAskTimeout: Timeout = 5.seconds
  def shutdown(): Unit = actor ! Shutdown
  def isAppReady(): Future[Boolean] = (actor ? IsAppReady).mapTo[Boolean]
}
