package design_principles.microservice.kafka_consumer_microservice

import akka.actor.ActorSystem
import akka.cluster.ClusterEvent
import akka.dispatchers.ActorsDispatchers
import akka.http.AkkaHttpServer
import akka.http.scaladsl.server.Directives._
import api.stats.ClusterStats
import com.typesafe.config.{Config, ConfigFactory}
import design_principles.actor_model.context_provider.{Guardian, GuardianRequirements}
import design_principles.application.Application
import life_cycle.AppLifecycleMicroservice
import monitoring.KamonMonitoring
import serialization.EventSerializer

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object MainApplication {

  def startMicroservices(
      microservices: Seq[KafkaConsumerMicroservice],
      ip: String,
      port: Int,
      actorSystemName: String,
      extraConfigurations: Config = ConfigFactory.empty
  ): Unit = {

    val mainConfig = ConfigFactory.load()
    lazy val config = Seq(
      mainConfig,
      ConfigFactory parseString EventSerializer.eventAdapterConf,
      ConfigFactory parseString EventSerializer.serializationConf,
      ConfigFactory parseString new ActorsDispatchers(mainConfig).actorsDispatchers,
      extraConfigurations
    ).reduce(_ withFallback _)

    val system = Guardian.getContext(GuardianRequirements(actorSystemName, config))
    val routes = ProductionMicroserviceContextProvider.getContext(system, config) { microserviceProvisioning =>
      val userRoutes = microservices.map(_.route(microserviceProvisioning)).reduce(_ ~ _)
      val systemRoutes = AppLifecycleMicroservice.route(microserviceProvisioning)
      val statRoutes = new ClusterStats()(system).route
      userRoutes ~ systemRoutes ~ statRoutes
    }
    AkkaHttpServer.start(routes, ip, port)(system)

    Await.result(system.whenTerminated, Duration.Inf)
  }
}
