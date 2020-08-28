package design_principles.microservice.cassandra_projectionist_microservice

import akka.actor.typed.ActorSystem
import akka.cluster.ClusterEvent
import akka.http.AkkaHttpServer
import akka.http.scaladsl.server.Directives._
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
      microservices: Seq[CassandraProjectionistMicroservice],
      ip: String,
      port: Int,
      actorSystemName: String,
      extraConfigurations: Config = ConfigFactory.empty
  ): Unit = {

    lazy val config = Seq(
      ConfigFactory.load(),
      ConfigFactory parseString EventSerializer.eventAdapterConf,
      ConfigFactory parseString EventSerializer.serializationConf,
      extraConfigurations
    ).reduce(_ withFallback _)

    implicit val monitoring = new KamonMonitoring

    val system = Guardian.getContext(GuardianRequirements(actorSystemName, config))
    val routes = ProductionMicroserviceContextProvider.getContext(system, monitoring) { microserviceProvisioning =>
      val userRoutes = microservices.map(_.route(microserviceProvisioning)).reduce(_ ~ _)
      val systemRoutes = AppLifecycleMicroservice.route(microserviceProvisioning)
      userRoutes ~ systemRoutes
    }
    AkkaHttpServer.start(routes, ip, port)(system)

    Await.result(system.whenTerminated, Duration.Inf)
  }
}
