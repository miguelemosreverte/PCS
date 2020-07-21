import play.api.libs.json.Format
import serialization.decode

import scala.io.Source
import scala.reflect.ClassTag

package object stubs {

  def loadExample[A <: ddd.ExternalDto: ClassTag](path: String)(implicit format: Format[A]): A = {
    val text = Source.fromFile(path).getLines mkString "\n"
    decode[A](text) match {
      case Left(explanation) =>
        // log.error(explanation)
        throw new Exception("Failed to load example")
      case Right(value) => value
    }
  }

}
