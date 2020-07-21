package spec

import scala.concurrent.ExecutionContextExecutor

import akka.actor.typed.ActorSystem
import akka.persistence.query.NoOffset
import akka.projection.eventsourced.EventEnvelope
import akka.{actor => classic}
import com.typesafe.config.{Config, ConfigFactory}
import infrastructure.cassandra.CassandraClient
import infrastructure.cassandra.CassandraTestkit.RowValidation
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import readside.proyectionists.common.infrastructure.Guardian
import serialization.EventSerializer
import utils.generators.Model.deliveryId

abstract class ProyectionistSpec[Events, MessageRoots]
    extends AnyFlatSpecLike
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with Eventually
    with IntegrationPatience
    with ScalaFutures {

  import ProyectionistSpec._
  implicit val ec: ExecutionContextExecutor = system.classicSystem.dispatcher
  implicit val classycSystem: classic.ActorSystem = system.classicSystem

  implicit def rowValidation: Map[String, String] => RowValidation =
    rowMessage => new RowValidation(rowMessage)

  def ProjectionTestkit: spec.consumers.ProjectionTestkit[Events, MessageRoots]

  def eventEnvelope(event: Events): EventEnvelope[Events] =
    EventEnvelope[Events](NoOffset, "", 1L, event, deliveryId)

  val db = new CassandraClient()
  def truncateTables(tables: Seq[String]): Unit = tables foreach { table =>
    println(s"Truncating $table")
    db.executeDDL(s"truncate read_side.$table")
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    println(s"""
         |
         |
         |
         |${Console.YELLOW}Starting tests for ${this.getClass.getName}${Console.RESET}
         |""".stripMargin)
  }
}

// @TODO Parallelize
object ProyectionistSpec {
  private val overrideConfig: Config =
    ConfigFactory.parseString("""
      akka.loglevel = INFO
      #akka.persistence.typed.log-stashing = on
      akka.actor.provider = cluster
      akka.remote.classic.netty.tcp.port = 0
      akka.remote.artery.canonical.port = 0
      akka.remote.artery.canonical.hostname = 127.0.0.1
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.journal.inmem.test-serialization = on
      akka.actor.allow-java-serialization = true
      #akka.cluster.jmx.multi-mbeans-in-same-jvm = on
    """)

  private val config: Config = Seq(
    ConfigFactory parseString EventSerializer.eventAdapterConf,
    ConfigFactory parseString EventSerializer.serializationConf,
    overrideConfig,
    ConfigFactory.load()
  ).reduce(_ withFallback _)

  implicit val system: ActorSystem[Nothing] =
    ActorSystem[Nothing](Guardian(), "PersonClassificationServiceReadSide", config)

  val db = new CassandraClient()(system.classicSystem)
  def truncateTables(tables: Seq[String]): Unit = tables foreach { table =>
    println(s"Truncating $table")
    db.executeDDL(s"truncate read_side.$table")
  }
}
