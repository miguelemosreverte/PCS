package design_principles.actor_model.utils

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import serialization.EventSerializer

object Generators {
  def actorSystem(port: Int = 2559,
                  actorSystemName: String = "PersonClassificationService",
                  extraConfig: Config = ConfigFactory.empty()): ActorSystem = {
    val customConf =
      ConfigFactory.parseString(s"""
      akka.loglevel = INFO
      #akka.persistence.typed.log-stashing = on
      akka.actor.provider = cluster
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.journal.inmem.test-serialization = on
      akka.actor.allow-java-serialization = true
      akka.cluster.jmx.multi-mbeans-in-same-jvm = on

      akka.cluster.seed-nodes = ["akka://$actorSystemName@0.0.0.0:$port"]
      akka.remote.artery.canonical.port = $port
      """)
    lazy val config: Config = Seq(
      ConfigFactory parseString EventSerializer.eventAdapterConf,
      ConfigFactory parseString EventSerializer.serializationConf,
      customConf,
      extraConfig, // maybe move up in position to override customConf
      ConfigFactory.load()
    ).reduce(_ withFallback _)

    ActorSystem(actorSystemName, config)
  }
}
