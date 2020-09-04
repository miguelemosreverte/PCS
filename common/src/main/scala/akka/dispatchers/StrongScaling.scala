package akka.dispatchers

import akka.dispatchers.StrongScaling.HardwareSpecs
import com.typesafe.config.{Config, ConfigFactory}

object StrongScaling {
  /*

  # Throughput (processedeMessagesPerActorPerThreadJump)
  # defines the maximum number of messages to be
  # processed per actor before the thread jumps to the next actor.
  # Set to 1 for as fair as possible.
   */
  case class HardwareSpecs(
      parallelismMin: Int,
      parallelismMax: Int,
      parallelismFactor: Int,
      processedMessagesPerActorPerThreadJump: Int
  )
  object HardwareSpecs {

    def apply(config: Config): HardwareSpecs = {

      val parallelismMin = config.getInt("hardwareSpecs.parallelismMin")
      val parallelismMax = config.getInt("hardwareSpecs.parallelismMax")
      val parallelismFactor = config.getInt("hardwareSpecs.parallelismFactor")
      val processedMessagesPerActorPerThreadJump = config.getInt("hardwareSpecs.processedMessagesPerActorPerThreadJump")

      HardwareSpecs(
        parallelismMin,
        parallelismMax,
        parallelismFactor,
        processedMessagesPerActorPerThreadJump
      )
    }
  }

  def apply(config: Config): StrongScaling = new StrongScaling(HardwareSpecs.apply(config))
}

class StrongScaling(hardwareSpec: HardwareSpecs) extends Dispatchers {

  def strongScalingDispatcher(dispatcherName: String) =
    s"${forkJoin(
      dispatcherName,
      1,
      4,
      1,
      hardwareSpec.processedMessagesPerActorPerThreadJump
    )}"

  def superhero(dispatcherName: String) =
    s"${forkJoin(
      dispatcherName,
      4,
      12,
      1,
      hardwareSpec.processedMessagesPerActorPerThreadJump
    )}"

  def strongScalingDispatcherCassandra = {
    val dispatcherName = "cassandraDispatcher"
    val partitionSize = 3000
    val resultSize = 1
    s"""
       |
       |akka.persistence {
       |  journal-plugin-fallback {
       |    circuit-breaker {
       |      max-failures = 10
       |      call-timeout = 1000s
       |      reset-timeout = 30s
       |    }
       |  }
       |}
    """.stripMargin
  }

}
