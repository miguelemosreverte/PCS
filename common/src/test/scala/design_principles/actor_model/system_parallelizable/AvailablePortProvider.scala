package design_principles.actor_model.system_parallelizable

import java.net.ServerSocket

import scala.io.Source
import scala.reflect.io.File
import java.io.FileOutputStream
import java.net.ServerSocket

object AvailablePortProvider {

  val maxReasonableAmmountOfTests = 5500

  // import ReservedPortRepository._

  var givenPorts: Set[Int] = Set.empty
  def port: Int = synchronized {
    val r = new scala.util.Random
    val availablePort = new ServerSocket(0).getLocalPort //r.between(2600, 2601 + maxReasonableAmmountOfTests)

    if (givenPorts contains availablePort)
      port
    else {
      givenPorts = givenPorts + availablePort
      println(Console.CYAN)
      println("givenPorts ")
      givenPorts.foreach(println)
      println(Console.RESET)
      availablePort
    }
  }
  /*
  object ReservedPortRepository {

    val record = s"/tmp/akka-ports-reserved-by-tests.txt"

    def maybeCreateFile(): Unit =
      new FileOutputStream(record, false).close()

    def givenPorts: Set[Int] = {
      maybeCreateFile()
      val source = Source.fromFile(record)
      val ports = source.getLines.toArray.map(_.toInt)
      source.close()
      if (ports.length > maxReasonableAmmountOfTests) reset
      ports.toSet
    }

    def addPort(port: Int): Unit =
      File(record).appendAll(port.toString)

    def reset: Boolean =
      File(record).delete()
  }*/

}
