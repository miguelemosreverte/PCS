package design_principles.actor_model.utils

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import serialization.EventSerializer

object Generators {
  def actorSystem(port: Int = 2559,
                  name: String = "PersonClassificationService",
                  extraConfig: Config = ConfigFactory.empty()): ActorSystem = {
    val randomAvailablePort = port
    val customConf =
      ConfigFactory.parseString(s"""
         akka.cluster.seed-nodes = ["akka://$name@0.0.0.0:$randomAvailablePort"]     
         akka.remote.artery.canonical.port = $randomAvailablePort
         """)
    lazy val config = Seq(
      ConfigFactory parseString EventSerializer.eventAdapterConf,
      ConfigFactory parseString EventSerializer.serializationConf,
      customConf,
      extraConfig, // maybe move up in position to override customConf
      ConfigFactory.load()
    ).reduce(_ withFallback _)

    ActorSystem(name, config)
  }
}
