package infrastructure.http

import scala.concurrent.Future
import scala.reflect.ClassTag
import akka.actor.ActorSystem
import akka.http.AkkaHttpClient
import akka.http.scaladsl.unmarshalling.Unmarshal
import play.api.libs.json.Format
import serialization.decode

class HttpClient {
  def GET[A: ClassTag](url: String)(implicit system: ActorSystem, format: Format[A]): Future[A] = {
    val validUrl = if (url contains "http://") url else s"http://$url"
    import system.dispatcher
    for {
      response <- new AkkaHttpClient().get(validUrl)
      body: String <- Unmarshal(response.entity).to[String]
    } yield {

      decode[A](body) match {
        case Left(value) => throw new Exception(value)
        case Right(value) => value
      }
    }
  }
}
