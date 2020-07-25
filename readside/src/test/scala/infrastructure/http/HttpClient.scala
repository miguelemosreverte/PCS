package infrastructure.http

import scala.concurrent.Future
import scala.reflect.ClassTag

import akka.AkkaHttpClient
import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.Unmarshal
import play.api.libs.json.Format
import serialization.decode

class HttpClient {
  def POST(url: String)(implicit system: ActorSystem): Future[Unit] = {
    val validUrl = if (url contains "http://") url else s"http://$url"
    import system.dispatcher
    for {
      _ <- new AkkaHttpClient().post(validUrl, "")
    } yield ()
  }

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
