package design_principles.microservice.kafka_consumer_microservice

import akka.actor.typed.ActorSystem
import akka.cluster.ClusterEvent
import akka.http.AkkaHttpServer
import akka.http.scaladsl.server.Directives._
import com.typesafe.config.{Config, ConfigFactory}
import design_principles.actor_model.context_provider.{GuardianRequirements, Guardian}
import life_cycle.AppLifecycleMicroservice
import serialization.EventSerializer

object MainApplication {

  def startMicroservices(
      microservices: Seq[KafkaConsumerMicroservice],
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
