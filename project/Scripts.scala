import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{FiniteDuration, _}
import scala.concurrent.{Await, Future, Promise}

import sbt._
import complete.DefaultParsers._

object Scripts extends AutoPlugin {

  object autoImport {
    lazy val devLiteUp = taskKey[Unit]("Start up dev-lite")
    lazy val devLiteDown = taskKey[Unit]("Take down dev-lite")
    lazy val kafkaProduce = inputKey[Unit]("Publish to Kafka topic")
    lazy val kafkaConsume = inputKey[Unit]("Consume to Kafka topic")
    lazy val describeTables = taskKey[Unit]("Describe the tables at Cassandra")
  }
  import autoImport._

  override lazy val projectSettings = Seq(
    devLiteUp := DockerCompose.Dev.up,
    devLiteDown := DockerCompose.Dev.down,
    kafkaProduce := {
      val args: Seq[String] = spaceDelimited("<arg>").parsed
      args foreach Kafka.publish
    },
    kafkaConsume := {
      val args: Seq[String] = spaceDelimited("<arg>").parsed
      args foreach Kafka.consume
    },
    describeTables := Cassandra.describeTables
  )
  // --------------------------------------------------------------------------

  import scala.sys.process._

  private def runScriptUntil(script: String,
                             untilTrace: String,
                             finiteDuration: FiniteDuration = 5 minutes): Boolean = {
    val promise = Promise[Boolean]()
    Future {
      println(s"Running $script")
      val process: Seq[String] = script.trim
        .split("&&")
        .map { _.trim }
        .map { Process(_) }
        .reduce(_ #&& _)
        .lineStream_!
      process.foreach { trace =>
        if (trace.contains(untilTrace)) {
          promise.success(true)
        }
      }
    }
    Await.result(promise.future, finiteDuration)
  }
  private def runScriptUntilEcho(script: String,
                                 untilTrace: String,
                                 finiteDuration: FiniteDuration = 5 minutes): Boolean =
    runScriptUntil(
      script = s"""
                  | $script && 
                  | echo $untilTrace
                  |""".stripMargin,
      untilTrace = untilTrace
    )

  private sealed trait DockerCompose {
    def ymlPath: String
    def down: Boolean = runScriptUntilEcho(
      script = s"docker-compose -f $ymlPath down -v",
      untilTrace = s"==docker-compose $ymlPath down finished=="
    )

    def up: Boolean = runScriptUntilEcho(
      script = s"docker-compose -f $ymlPath up -d",
      untilTrace = s"==docker-compose $ymlPath up finished=="
    )

    def logs(containerName: String, untilTrace: String): Boolean = runScriptUntil(
      script = s"""
           | cd ${ymlPath.replace("/docker-compose.yml", "")} && \
           | docker-compose logs -f $containerName
           |""".stripMargin,
      untilTrace = untilTrace
    )
  }
  private object DockerCompose {
    case object Dev extends DockerCompose {
      val ymlPath: String = "assets/docker-compose/dev-lite/docker-compose.yml"
    }
    case object Prod extends DockerCompose {
      val ymlPath: String = "assets/docker-compose/vm/docker-compose.yml"
    }
    case object K8s extends DockerCompose {
      val ymlPath: String = "assets/k8s/docker-compose.yml"
    }
  }
  private case object Kafka {
    def publish(topic: String): Boolean = runScriptUntilEcho(
      script = s"kafkacat -b 0.0.0.0:9092 -t $topic -P ./assets/examples/$topic.json",
      untilTrace = s"==kafkacat published $topic finished=="
    )
    def consume(topic: String): Boolean = runScriptUntilEcho(
      script = s"kafkacat -C -b 0.0.0.0:9092 -t $topic",
      untilTrace = s"==kafkacat consuming $topic finished=="
    )
  }
  private case object Cassandra {
    private def callCassandra(stmt: String): String =
      s"docker exec cassandra cqlsh -e '$stmt;'"

    def cqlsh(stmt: String): String =
      Process(Seq("docker", "exec", "cassandra", "cqlsh", "-e", stmt)) !!

    def describeTables: Unit = println(cqlsh("desc tables;"))

    def select(table: String): Unit = println(cqlsh(s"select * from $table;"))

  }
  private case object Api {
    private def curlApi(uri: String): Boolean = runScriptUntilEcho(
      script = s"curl http://0.0.0.0:8081/state$uri",
      untilTrace = s"==curl $uri finished=="
    )

    def curlApiSujeto(sujetoId: String): Boolean =
      curlApi(s"/sujeto/$sujetoId")
    def curlApiObjeto(sujetoId: String, objetoId: String): Boolean =
      curlApi(s"/sujeto/$sujetoId/objeto/$objetoId")
    def curlApiObligacion(sujetoId: String, objetoId: String, obligacionId: String): Boolean =
      curlApi(s"/sujeto/$sujetoId/objeto/$objetoId/obligacion/$obligacionId")
  }

  private case object PCS {
    def runReadSide: Boolean = runScriptUntil(
      script = s"""
           | export SEED_NODES = "akka://PersonClassificationService@0.0.0.0:2551" && \
           | sbt run
           |""".stripMargin,
      untilTrace = "[new]"
    )

    def run: Boolean = runScriptUntil(
      script = "sbt 'runMain readside.ReadSide'",
      untilTrace = "#Eventprocessor"
    )
  }

}
