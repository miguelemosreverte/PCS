package design_principles.actor_model.system_parallelizable

import akka.actor.{ActorRef, ActorSystem}
import design_principles.actor_model.ActorSpec
import java.net.ServerSocket
import design_principles.actor_model.utils.Generators

object ActorSystemParallelizerBuilder {

  private val system = Generators.actorSystem(new ServerSocket(0).getLocalPort, "ActorSystemParallelizerBuilder")
  private val actorSystemNumbers = 3551 to 3555
  lazy val actor: ActorRef =
    system.actorOf(ActorSystemParallelizer.props(actorSystemNumbers), s"acceptance-test-actor")
}
