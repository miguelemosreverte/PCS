package design_principles.actor_model.mechanism.stream_supervision

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransactionController
import akka.http.Controller
import akka.http.scaladsl.server.Directives.{complete, path, pathPrefix, post, _}
import design_principles.microservice.kafka_consumer_microservice.KafkaConsumerMicroserviceRequirements

class MessageProcessorSupervisorActorController(
    streams: Set[ActorTransactionController]
)(
    implicit
    requirements: KafkaConsumerMicroserviceRequirements
) extends Controller(requirements.monitoring) {

  private implicit val system: ActorSystem = requirements.ctx

  val startStopSingleton: ActorRef = StartStopSingleton.start

  val streamsSupervisor: ActorRef =
    system.actorOf(Props(new MessageProcessorSupervisorActor(startStopSingleton, streams)))

  def startAll(): Unit = startStopSingleton ! StartStopSingleton.Start()

  def stopAll(): Unit = startStopSingleton ! StartStopSingleton.Stop()

  def stop_kafka: Route =
    post {
      path("stop") {
        handleErrors(exceptionHandler) {
          stopAll()
          requests.increment()
          complete(s"Stopping all kafka consumers on all nodes")
        }
      }
    }

  def start_kafka: Route =
    post {
      path("start") {
        handleErrors(exceptionHandler) {
          startAll()
          requests.increment()
          complete(s"Starting all kafka consumers on all nodes")
        }
      }
    }

  def route: Route =
    pathPrefix("kafka") {
      start_kafka ~ stop_kafka
    }
}
