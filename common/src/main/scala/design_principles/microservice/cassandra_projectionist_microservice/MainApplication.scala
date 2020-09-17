package design_principles.microservice.cassandra_projectionist_microservice

import akka.actor.typed.ActorSystem
import akka.cluster.ClusterEvent
import akka.http.AkkaHttpServer
import akka.http.scaladsl.server.Directives._
import com.typesafe.config.{Config, ConfigFactory}
import config.StaticConfig
import design_principles.actor_model.context_provider.{Guardian, GuardianRequirements}
import design_principles.application.Application
import life_cycle.AppLifecycleMicroservice
import monitoring.KamonMonitoring
import serialization.EventSerializer

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object MainApplication {

  def startMicroservices(
      microservicesFactory: CassandraProjectionistMicroserviceRequirements => Seq[CassandraProjectionistMicroservice],
      ip: String,
      port: Int,
      actorSystemName: String,
      extraConfigurations: Config = ConfigFactory.empty
  ): Unit = {

    lazy val config = StaticConfig.config

    implicit val monitoring = new KamonMonitoring

    val system = Guardian.getContext(GuardianRequirements(actorSystemName, config))
    val routes = ProductionMicroserviceContextProvider.getContext(system, config) { implicit microserviceProvisioning =>
      val microservices = microservicesFactory(microserviceProvisioning)
      val userRoutes = microservices.map(_.route).reduce(_ ~ _)
      val systemRoutes = (new AppLifecycleMicroservice).route
      userRoutes ~ systemRoutes
    }
    AkkaHttpServer.start(routes, ip, port)(system)

    Await.result(system.whenTerminated, Duration.Inf)
  }
}
