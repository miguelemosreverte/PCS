package akka.entity

import scala.concurrent.Await

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.cluster.singleton.{
  ClusterSingletonManager,
  ClusterSingletonManagerSettings,
  ClusterSingletonProxy,
  ClusterSingletonProxySettings
}
import akka.util.Timeout

trait SingletonEntity[Requirements] extends ClusterEntity[Requirements] {

  def props(requirements: Requirements): Props

  def startWithRequirements(requirements: Requirements)(implicit system: ActorSystem): ActorRef = {

    import system.dispatcher

    import scala.concurrent.duration._
    implicit val timeout: Timeout = Timeout(10 seconds)
    val actorRef = system.actorSelection(s"/user/${typeName}Proxy").resolveOne()

    Await.result(
      actorRef.recover {
        case ex: Throwable =>
          system.actorOf(
            ClusterSingletonManager.props(
              props(requirements),
              PoisonPill,
              ClusterSingletonManagerSettings(system)
            ),
            typeName
          )
          system.actorOf(
            ClusterSingletonProxy.props(singletonManagerPath = s"/user/${typeName}",
                                        settings = ClusterSingletonProxySettings(system)),
            name = s"${typeName}Proxy"
          )
      },
      10 seconds
    )

  }

}
