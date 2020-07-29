package spec.testsuite

import scala.concurrent.Await

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorPath, ActorSelection, ActorSystem, Identify}
import infrastructure.cassandra.CassandraClient
import infrastructure.cassandra.CassandraTestkit.RowValidation
import readside.proyectionists.common.infrastructure.Guardian
import spec.testkit.ProjectionTestkit
import akka.pattern.ask
import scala.concurrent.duration._
import scala.util.{Failure, Success}

import akka.util.Timeout

abstract class ProjectionTestContext[Events, MessageRoots](implicit system: ActorSystem) {
  def ProjectionTestkit: ProjectionTestkit[Events, MessageRoots]

  implicit def rowValidation: Map[String, String] => RowValidation =
    rowMessage => new RowValidation(rowMessage)

  // @TODO Create or Find Guardian (this should go in acceptance)
  // val guardian: Unit = {
  //   implicit val timeout: Timeout = 3 seconds
  //   val guardianRef: ActorSelection =
  //     system.actorSelection(
  //       ActorPath.fromString("akka://ActorSystemParallelized/system/PersonClassificationServiceReadSide")
  //     )

  //   guardianRef.ask(Identify).onComplete {
  //     case Failure(exception) =>
  //       println(s"Guardian Failure($exception)")
  //       // system.toTyped.systemActorOf[Nothing](Guardian(), "PersonClassificationServiceReadSide")
  //     case Success(value) =>
  //       println(s"Guardian Success($value)")
  //   }(system.dispatcher)
  // }

  val db = new CassandraClient()
  def truncateTables(tables: Seq[String]): Unit = tables foreach { table =>
    println(s"Truncating $table")
    db.executeDDL(s"truncate read_side.$table")
  }

  def close(): Unit = {
    db.close()
  }
}
