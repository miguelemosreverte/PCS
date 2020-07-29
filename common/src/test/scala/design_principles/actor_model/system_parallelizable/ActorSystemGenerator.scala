package design_principles.actor_model.system_parallelizable

import java.net.ServerSocket

import akka.actor.{Actor, ActorSystem, Stash}
import akka.pattern.pipe
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Future
import scala.math.random
import scala.util.Try

class ActorSystemGenerator extends Actor {

  import ActorSystemGenerator._
  import context.dispatcher

  implicit private val _system: ActorSystem = context.system

  override def receive: Receive = {
    case RunTest(config, test) =>
      val r = new scala.util.Random
      val availablePort = r.between(2600, 3000)
      val actorSystemConfig = customConf(availablePort) withFallback config

      (0 to 10) foreach { _ =>
        println("HERE, creating ActorSystem! " + s"$actorSystemName-$availablePort")
      }
      val system = ActorSystem(s"$actorSystemName-$availablePort", actorSystemConfig)

      (0 to 10) foreach { _ =>
        println("HERE, processTest -- " + s"$actorSystemName-$availablePort")
      }
      processTest(test, system)
    case unexpected =>
      (0 to 10) foreach { _ =>
        println("HERE, WHYYYYYYYY -- " + s"$actorSystemName" + s"--- ${unexpected}")
      }

  }

  private def processTest(
      test: ActorSystem => Unit,
      actorSystem: ActorSystem
  ): Unit = {

    val testResult = for {
      result <- Future(Try(test(actorSystem)))
    } yield result

    testResult.onComplete { _ =>
      // actorSystem.terminate()
    }
    testResult.pipeTo(sender())
  }

}

object ActorSystemGenerator {
  private val actorSystemName = "ActorSystemGenerator"

  def customConf(port: Int): Config =
    ConfigFactory.parseString(s"""
      akka.cluster.seed-nodes = ["akka://$actorSystemName@0.0.0.0:$port"]
      akka.remote.artery.canonical.port = $port
     """)

  case class RunTest(config: Config, test: ActorSystem => Unit)
}
