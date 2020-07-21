package design_principles.actor_model.system_parallelizable

import akka.actor.{ActorRef, ActorSystem}

object ActorSystemParallelizerBuilder {
  private val system = ActorSystem("ActorSystemParallelizerBuilder")
  private val actorSystemNumbers = 3551 to 3555
  lazy val actor: ActorRef =
    system.actorOf(ActorSystemParallelizer.props(actorSystemNumbers), s"acceptance-test-actor")
}
