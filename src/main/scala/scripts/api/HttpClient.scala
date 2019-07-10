package scripts.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

sealed trait HttpClient {
  def executeRequest(req: HttpRequest): Future[HttpResponse]
}

case class AkkaHttpClient(headers: Seq[HttpHeader])(implicit sys: ActorSystem) extends HttpClient with LazyLogging {

  private def addHeaders(req: HttpRequest): HttpRequest = {
    headers.foldLeft(req)((req, h) => req.addHeader(h))
  }

  def executeRequest(req: HttpRequest): Future[HttpResponse] = {
    logger.debug(s"Req: => $req")
    Http().singleRequest(addHeaders(req))
  }
}
