package api.stats

import akka.actor.{Actor, ActorRef, ActorSystem, Address, Props}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberEvent, MemberRemoved, MemberUp}
import akka.cluster.{Cluster, MemberStatus}
import akka.entity.ShardedEntity.NoRequirements
import akka.entity.SingletonEntity
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, OK}
import akka.http.scaladsl.server.Directives.{complete, get, path, pathPrefix}
import akka.http.scaladsl.server.Route

import scala.collection.Set

class ClusterStats(implicit system: ActorSystem) {

  val memberListener: ActorRef = MemberListener.startWithRequirements(NoRequirements())

  import system.dispatcher

  val members =
    get {
      path("members") {
        complete {
          (memberListener
            .ask[Set[Address]](GetNodes))
            .map { answer =>
              HttpResponse(OK, entity = answer.toString)
            }
            .recover { case e: Exception => HttpResponse(InternalServerError, entity = e.getMessage) }
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
