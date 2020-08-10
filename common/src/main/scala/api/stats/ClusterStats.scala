package api.stats

import akka.actor.typed.scaladsl.ActorContext
import akka.actor.{Actor, ActorRef, ActorSystem, Address, Props}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberEvent, MemberRemoved, MemberUp}
import akka.cluster.{Cluster, MemberStatus}
import akka.entity.ShardedEntity.NoRequirements
import akka.entity.SingletonEntity
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, OK}
import akka.http.scaladsl.server.Directives.{complete, get, path, pathPrefix}
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import design_principles.microservice.Microservice
import design_principles.microservice.kafka_consumer_microservice.KafkaConsumerMicroservice
import akka.actor.typed.scaladsl.adapter._
import utils.Inference.{getSimpleName, getSubtypesOf, resolveClassHierarchy}

import scala.collection.Set
import scala.reflect.ClassTag

class ClusterStats(implicit ctx: ActorContext[MemberUp]) {

  //val memberListener: ActorRef = ctx.spawn()

  implicit val ec = ctx.system.toClassic.dispatcher

  val members =
    get {
      path("members") {
        complete {
          // memberListener
          // .ask[Set[Address]](GetNodes)
          //.map { answer =>
          HttpResponse(
            OK,
            entity = {

              val microservices =
                getSubtypesOf[KafkaConsumerMicroservice]().map(_.getCanonicalName).toArray
              // every application main should be able to tell its components in a procedural manner.
              // thus, cluster stats should scrap by application.

              val microserviceNameAndKafkaConsumers: Map[String, Set[String]] =
                resolveClassHierarchy[KafkaConsumerMicroservice, ActorTransaction[_]].map {
                  case (key, value) =>
                    (getSimpleName(key.getName).replace("Microservice", ""),
                     value.map(_.getName).map(getSimpleName(_).replace("Transaction", "")))
                }

              case class ActorTransactionDescription(name: String, `type`: String = "entity") {
                override def toString =
                  s"{'name': '${name}', 'type':'${`type`}', 'children': [] }"
              }
              case class MicroserviceDescription(name: String,
                                                 `type`: String = "shard",
                                                 children: Set[ActorTransactionDescription]) {
                override def toString =
                  s"{'name': '${name}', 'type':'${`type`}', 'children': [${children.map(_.toString).mkString(",")}] }"
              }
              case class NodesDescription(name: String,
                                          `type`: String = "member",
                                          children: Set[MicroserviceDescription]) {
                override def toString =
                  s"{'name': '${name}', 'type':'${`type`}', 'children': [${children.map(_.toString).mkString(",")}] }"
              }
              case class ClusterDescription(name: String, `type`: String = "cluster", children: Set[NodesDescription]) {
                override def toString =
                  s"{'name': '${name}', 'type':'${`type`}', 'children': [${children.map(_.toString).mkString(",")}] }"
              }

              ClusterDescription(
                name = "PersonClassificationService",
                children = (1 to 3).toSet.map { nodeName: Int =>
                  NodesDescription(
                    name = s"node#$nodeName",
                    children = microserviceNameAndKafkaConsumers.keys.toSet.map { microserviceName: String =>
                      MicroserviceDescription(
                        name = s"$microserviceName#$nodeName",
                        children = microserviceNameAndKafkaConsumers(microserviceName).map { actorTransactionName =>
                          ActorTransactionDescription(
                            name = s"$actorTransactionName#$nodeName"
                          )
                        }
                      )
                    }
                  )
                }
              ).toString

            }
            //answer.toString)
          )
          //}
          //.recover { case e: Exception => HttpResponse(InternalServerError, entity = e.getMessage) }
        }

      }
    }

  val route: Route =
    pathPrefix("akka") {
      members
    }

  case object GetNodes

  class MemberListener extends Actor {

    val cluster = Cluster(context.system)

    override def preStart(): Unit =
      cluster.subscribe(self, classOf[MemberEvent])

    override def postStop(): Unit =
      cluster unsubscribe self

    var nodes = Set.empty[Address]

    def receive = {
      case state: CurrentClusterState =>
        nodes = state.members.collect {
          case m if m.status == MemberStatus.Up => m.address
        }
      case MemberUp(member) =>
        nodes = nodes.union(Set(member.address))
      case MemberRemoved(member, _) =>
        nodes = nodes.diff(Set(member.address))
      case _: MemberEvent => // ignore
      case GetNodes =>
        sender ! nodes
    }
  }

  object MemberListener extends SingletonEntity[NoRequirements] {
    override def props(requirements: NoRequirements): Props = Props(new MemberListener)
  }

}
