package config

//import akka.dispatchers.{ActorsDispatchers, StrongScaling}
import com.typesafe.config.{Config, ConfigFactory}
import serialization.EventSerializer

object StaticConfig {
  private val mainConfig = ConfigFactory.load()
  lazy val config = Seq(
    mainConfig,
    ConfigFactory parseString EventSerializer.eventAdapterConf,
    ConfigFactory parseString EventSerializer.serializationConf,
    //ConfigFactory parseString new ActorsDispatchers(mainConfig).actorsDispatchers
    //ConfigFactory parseString StrongScaling.apply(mainConfig).strongScalingDispatcherCassandra
  ).reduce(_ withFallback _)

  val ETL_mode = "on"
  if (ETL_mode == "on") {
    config.withFallback(
      ConfigFactory.parseString(
        """
          |
          |      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
          |""".stripMargin)
    )
  } else config
}
