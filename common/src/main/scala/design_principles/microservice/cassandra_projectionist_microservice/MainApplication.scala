package design_principles.microservice.cassandra_projectionist_microservice

import akka.actor.typed.ActorSystem
import akka.cluster.ClusterEvent
import akka.http.AkkaHttpServer
import akka.http.scaladsl.server.Directives._
import com.typesafe.config.{Config, ConfigFactory}
import design_principles.actor_model.context_provider.{Guardian, GuardianRequirements}
import design_principles.application.Application
import life_cycle.AppLifecycleMicroservice
import serialization.EventSerializer

object MainApplication
    extends Application[CassandraProjectionistMicroserviceRequirements, CassandraProjectionistMicroservice] {

  def startMicroservices(
      microservices: Seq[CassandraProjectionistMicroservice],
      ip: String,
      port: Int,
      actorSystemName: String,
      extraConfigurations: Config = ConfigFactory.empty
  ): ActorSystem[ClusterEvent.MemberUp] = {

    lazy val config = Seq(
      ConfigFactory.load(),
      ConfigFactory parseString EventSerializer.eventAdapterConf,
      ConfigFactory parseString EventSerializer.serializationConf,
      extraConfigurations
    ).reduce(_ withFallback _)

    Guardian.getContext(GuardianRequirements(actorSystemName, config)) { akkaNodeIsUp =>
      val routes = ProductionMicroserviceContextProvider.getContext(akkaNodeIsUp) { microserviceProvisioning =>
        val userRoutes = microservices.map(_.route(microserviceProvisioning)).reduce(_ ~ _)
        val systemRoutes = AppLifecycleMicroservice.route(microserviceProvisioning)
        userRoutes ~ systemRoutes
      }
      AkkaHttpServer.start(routes, ip, port)(akkaNodeIsUp)
    }
  }
}
