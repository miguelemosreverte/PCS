package design_principles.structure

import akka.actor.typed.ActorSystem
import akka.cluster.ClusterEvent
import akka.http.AkkaHttpServer
import com.typesafe.config.{Config, ConfigFactory}
import design_principles.actor_model.context_provider.{ActorSystemContextProvider, ActorSystemRequirements}
import design_principles.microservice.Microservice
import design_principles.microservice.context_provider.ProductionMicroserviceContextProvider
import life_cycle.AppLifecycleMicroservice
import akka.http.scaladsl.server.Directives._
import serialization.EventSerializer

object MainApplication {

  def startMicroservices(
      microservices: Seq[Microservice],
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

    ActorSystemContextProvider.getContext(ActorSystemRequirements(actorSystemName, config)) { akkaNodeIsUp =>
      val routes = ProductionMicroserviceContextProvider.getContext(akkaNodeIsUp) { microserviceProvisioning =>
        val userRoutes = microservices.map(_.route(microserviceProvisioning)).reduce(_ ~ _)
        val systemRoutes = AppLifecycleMicroservice.route(microserviceProvisioning)
        userRoutes ~ systemRoutes
      }
      AkkaHttpServer.start(routes, ip, port)(akkaNodeIsUp)
    }
  }

}
