package design_principles.actor_model.system_parallelizable

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import serialization.EventSerializer

object ActorSystemParallelizerState {
  private val actorSystemName = "ActorSystemParallelized"

  private lazy val config: Config = Seq(
    ConfigFactory parseString EventSerializer.eventAdapterConf,
    ConfigFactory parseString EventSerializer.serializationConf,
    ConfigFactory.load()
  ).reduce(_ withFallback _)

  def fromActorSystemNumbers(actorSystemNumbers: Seq[Int]): ActorSystemParallelizerState = {
    val allAvailableActorSystems = actorSystemNumbers.map { port =>
      val actorSystemConfig = customConf(port) withFallback config
      val actorSystem = ActorSystem(actorSystemName, actorSystemConfig)
      port -> actorSystem
    }.toMap
    ActorSystemParallelizerState(allAvailableActorSystems)
  }

  private def customConf(port: Int): Config =
    ConfigFactory.parseString(s"""
      akka.cluster.seed-nodes = ["akka://$actorSystemName@0.0.0.0:$port"]
      akka.remote.artery.canonical.port = $port
     """)
}

case class ActorSystemParallelizerState(freeActorSystems: Map[Int, ActorSystem]) {
  def nextFreeActorSystem: Option[(Int, ActorSystem)] =
    freeActorSystems.headOption

  def withBusyActorSystem(operationalActorSystem: (Int, ActorSystem)): ActorSystemParallelizerState = {
    val withoutBusyActorSystem = freeActorSystems - operationalActorSystem._1
    copy(freeActorSystems = withoutBusyActorSystem)
  }

  def withFreeActorSystem(operationalActorSystem: (Int, ActorSystem)): ActorSystemParallelizerState =
    copy(freeActorSystems = freeActorSystems + operationalActorSystem)
}
