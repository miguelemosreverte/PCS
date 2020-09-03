package cqrs.base_actor.untyped

import akka.Done
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.alpakka.cassandra.CassandraSessionSettings
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSession, CassandraSessionRegistry}
import com.datastax.oss.driver.api.core.cql.PreparedStatement
import cqrs.base_actor.untyped.SaveToCassandraActor.{saveToCassandraActor, session, SerializedEvent}
import design_principles.actor_model.Event
import org.slf4j.LoggerFactory

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
object SaveToCassandraActor {
  case class SerializedEvent(aggregateRoot: String, json: String)

  private val sessionSettings = CassandraSessionSettings.create()
  private implicit var session: Option[CassandraSession] = None
  private def getSession(system: ActorSystem): CassandraSession = {
    session match {
      case None => {
        val newSession = CassandraSessionRegistry.get(system).sessionFor(sessionSettings)
        session = Some(newSession)
        newSession
      }
      case Some(session) => session

    }
  }

  private val logger = LoggerFactory.getLogger(this.getClass)

  final private var statement: Option[PreparedStatement] = None
  private def getStatement(system: ActorSystem): PreparedStatement = {
    statement match {
      case None => {
        val newStatement: PreparedStatement = {
          Await.result(
            getSession(system).prepare(s"""
              INSERT INTO eventsourcing.events 
              (aggregateRoot, event) 
              VALUES (?, ?);
            """.stripMargin),
            60 seconds
          )
        }
        statement = Some(newStatement)
        newStatement
      }
      case Some(statement) => statement

    }
  }

  var saveToCassandraActor: Option[ActorRef] = None
  def getSaveToCassandraActor(s: ActorSystem) =
    saveToCassandraActor match {
      case Some(saveToCassandraActor) => saveToCassandraActor
      case None =>
        val newActor = s.actorOf(Props(new SaveToCassandraActor()))
        saveToCassandraActor = Some(newActor)
        newActor

    }
}
class SaveToCassandraActor extends Actor {

  import akka.pattern.pipe
  implicit val ec: ExecutionContext = context.system.dispatcher
  override def receive: Receive = {
    case SerializedEvent(aggregateRoot, serializedEvent) =>
      SaveToCassandraActor
        .getSession(context.system)
        .executeWrite(SaveToCassandraActor.getStatement(context.system).bind(aggregateRoot, serializedEvent))
        .pipeTo(self)
    case Done => ()

  }
}
