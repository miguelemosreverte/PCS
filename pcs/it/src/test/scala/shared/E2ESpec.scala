package shared

import scala.concurrent.Future
import scala.io.Source

import akka.Done
import com.typesafe.config.ConfigFactory
import design_principles.actor_model.ActorSpec
import infrastructure.cassandra.CassandraClient
import infrastructure.http.HttpClient
import infrastructure.kafka.KafkaClient
import org.slf4j.{Logger, LoggerFactory}

trait E2ESpec extends ActorSpec {

  /*
  val kafka = new KafkaClient(ConfigFactory.load())
  val http = new HttpClient()
  val db = new CassandraClient()
  val log: Logger = LoggerFactory.getLogger(this.getClass)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    val path = "assets/scripts/cassandra/truncate_cassandra.cql"
    val source = Source.fromFile(path)
    val executionResult: Future[Iterator[Done]] = Future.sequence(source.getLines map db.executeDDL)
    executionResult.onComplete { _ =>
      source.close()
    }
  }

  def printStage(stageInfo: String): Unit = {
    log.info(Console.CYAN_B + stageInfo + Console.RESET)
  }
 */
}
