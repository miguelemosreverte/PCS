package design_principles.actor_model.system_parallelizable

import akka.actor.{ActorRef, ActorSystem, Props}
import design_principles.actor_model.ActorSpec
import java.net.ServerSocket

import design_principles.actor_model.utils.Generators

object ActorSystemParallelizerBuilder {

  def availablePort = new ServerSocket(0).getLocalPort
  private val system = Generators.actorSystem(availablePort, "ActorSystemParallelizerBuilder")
  //private val actorSystemNumbers: Seq[Int] = (1 to 5) map (_ => availablePort)
  println("GOING TO START acceptance-test-actor")
  lazy val actor: ActorRef =
    system.actorOf(Props(new ActorSystemGenerator), s"acceptance-test-actor")
  //system.actorOf(ActorSystemParallelizer.props(actorSystemNumbers), s"acceptance-test-actor")
}
