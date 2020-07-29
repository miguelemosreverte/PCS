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
      availablePort
    }
  }

}
