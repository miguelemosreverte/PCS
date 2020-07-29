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
      akka.loglevel = INFO
      #akka.persistence.typed.log-stashing = on
      akka.actor.provider = cluster
      akka.remote.classic.netty.tcp.port = 0
      akka.remote.artery.canonical.port = 0
      akka.remote.artery.canonical.hostname = 127.0.0.1
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.journal.inmem.test-serialization = on
      akka.actor.allow-java-serialization = true
      akka.cluster.jmx.multi-mbeans-in-same-jvm = on
      
    
         """)
    lazy val config: Config = Seq(
      ConfigFactory parseString EventSerializer.eventAdapterConf,
      ConfigFactory parseString EventSerializer.serializationConf,
      customConf
    ).reduce(_ withFallback _).resolve()

    ActorSystem(name, config)
  }
}
