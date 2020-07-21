package kafka

import ai.x.play.json.Jsonx
import akka.Done
import akka.actor.{Actor, Props}
import akka.entity.SingletonEntity
import akka.stream.UniqueKillSwitch
import kafka.StopStartKafka.StartStopKafkaRequirements
import org.slf4j.LoggerFactory
import play.api.libs.json.Format

class StopStartKafkaActor(
    requirements: StartStopKafkaRequirements
) extends Actor {

  import StopStartKafkaActor._

  implicit val transactionRequirements = requirements.transactionRequirements

  implicit val system = context.system
  import system.dispatcher

  var isKafkaStarted: Boolean = false
  var killSwitches: UniqueKillSwitch = null
  val log = LoggerFactory.getLogger(this.getClass)

  val topic = requirements.actorTransactions.topic
  def startTransaction(): UniqueKillSwitch = {
    log.info(s"Starting transactional consumer for $topic")
    val (killSwitch, done) = new KafkaTransactionalMessageProcessor()
      .run(topic, s"${topic}SINK", message => {
        requirements.actorTransactions.transaction(message).map { output: akka.Done =>
          Seq(output.toString)
        }
      })
    done.onComplete { result =>
      if (isKafkaStarted) {
        // restart if the flag is set to true
        log.warn(s"Transaction finished with ${result}. Restarting it.")
        startTransaction()
      }
    }
    killSwitch
  }

  override def receive: Receive = {
    case s: StartKafka if !isKafkaStarted =>
      log.info("StartKafka")
      killSwitches = startTransaction()
      isKafkaStarted = true
      sender ! Done
    case s: StartKafka if isKafkaStarted =>
      self ! StopKafka
      self ! StartKafka
    case s: StopKafka if isKafkaStarted =>
      killSwitches.shutdown()
      log.info("Stopped Kafka")
      killSwitches = null
      isKafkaStarted = false
      sender ! Done

    case other =>
      // @TODO FIX THIS => unexpected !!
      log.info(s"Received unexpected message at StopStartActor for ${topic} | $other")

  }
}
object StopStartKafkaActor extends SingletonEntity[StartStopKafkaRequirements] {
  override def props(requirements: StartStopKafkaRequirements): Props = Props(new StopStartKafkaActor(requirements))

  case class StartKafka(message: String = "StartKafka")
  object StartKafka {
    implicit val start_kafkaS: Format[StartKafka] = Jsonx.formatCaseClass[StartKafka]
  }
  case class StopKafka(message: String = "StopKafka")
  object StopKafka {
    implicit val stop_kafkaS: Format[StopKafka] = Jsonx.formatCaseClass[StopKafka]
  }
}
