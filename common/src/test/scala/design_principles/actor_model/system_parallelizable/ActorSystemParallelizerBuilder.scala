package design_principles.actor_model.system_parallelizable

import akka.actor.{ActorRef, ActorSystem, Props}
import design_principles.actor_model.ActorSpec
import java.net.ServerSocket

import design_principles.actor_model.utils.Generators

object ActorSystemParallelizerBuilder {

  def availablePort = new ServerSocket(0).getLocalPort
  private val system = Generators.actorSystem(availablePort, "ActorSystemParallelizerBuilder")
  lazy val actor: ActorRef =
    system.actorOf(Props(new ActorSystemGenerator), s"acceptance-test-actor")
}
