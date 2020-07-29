package design_principles.actor_model.system_parallelizable

import akka.actor.{Actor, ActorSystem}
import akka.pattern.pipe
import com.typesafe.config.{Config, ConfigFactory}
import design_principles.actor_model.ActorSpec
import scala.collection.mutable
import scala.concurrent.Future
import scala.util.Try

class ActorSystemGenerator extends Actor {

  import ActorSystemGenerator._
  import context.dispatcher

  implicit private val _system: ActorSystem = context.system

  override def postStop(): Unit =
    childSystems.foreach(_.terminate())

  val childSystems: mutable.ListBuffer[ActorSystem] = mutable.ListBuffer.empty[ActorSystem]

  override def receive: Receive = {

    case RunTest(config, test) =>
      val r = new scala.util.Random
      val availablePort = r.between(2600, 3000)
      val actorSystemConfig = customConf(availablePort) withFallback config

      val system = ActorSpec.system //ActorSystem(s"$actorSystemName-$availablePort", actorSystemConfig)
      childSystems.addOne(system)

      processTest(test, system)

  }

  private def processTest(
      test: ActorSystem => Unit,
      actorSystem: ActorSystem
  ): Unit = {

    val testResult = for {
      result <- Future(Try(test(actorSystem)))
    } yield result

    testResult.pipeTo(sender())
  }

}

object ActorSystemGenerator {
  val actorSystemName = "ActorSystemGenerator"

  def customConf(port: Int): Config =
    ConfigFactory.parseString(s"""
      akka.cluster.seed-nodes = ["akka://$actorSystemName@0.0.0.0:$port"]
      akka.remote.artery.canonical.port = $port
     """)

  case class RunTest(config: Config, test: ActorSystem => Unit)
}
