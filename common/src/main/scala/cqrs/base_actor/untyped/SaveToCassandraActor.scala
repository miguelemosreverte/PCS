package cqrs.base_actor.untyped

import akka.Done
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.alpakka.cassandra.CassandraSessionSettings
import akka.stream.alpakka.cassandra.scaladsl.{CassandraSession, CassandraSessionRegistry}
import com.datastax.oss.driver.api.core.cql.PreparedStatement
import cqrs.base_actor.untyped.SaveToCassandraActor.{SerializedEvent, saveToCassandraActor, session, statement}
import design_principles.actor_model.Event
import org.slf4j.LoggerFactory
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
object SaveToCassandraActor {
  case class SerializedEvent(aggregateRoot: String, json: String)

  private implicit val session: CqlSession = CqlSession.builder().build()
  final private val statement: PreparedStatement = session.prepare(s"""
              INSERT INTO eventsourcing.events 
              (aggregateRoot, event) 
              VALUES (?, ?);
            """.stripMargin)


import scala.compat.java8.FutureConverters._
class SaveToCassandraActor extends Actor {

  import akka.pattern.pipe
  implicit val ec: ExecutionContext = context.system.dispatcher
  override def receive: Receive = {
    case SerializedEvent(aggregateRoot, serializedEvent) =>
      session
        .executeAsync(statement.bind(aggregateRoot, serializedEvent))
        .toScala
        .map { _ =>
          Done
        } pipeTo(self)
    case Done => ()

  }
}
