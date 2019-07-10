package scripts.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

sealed trait HttpClient {
  def executeRequest(req: HttpRequest)(implicit ec: ExecutionContext): Future[HttpResponse]
}

case class AkkaHttpClient(headers: Seq[HttpHeader])(implicit sys: ActorSystem) extends HttpClient with LazyLogging {

  private def addHeaders(req: HttpRequest): HttpRequest = {
    headers.foldLeft(req)((req, h) => req.addHeader(h))
  }

  def executeRequest(req: HttpRequest)(implicit ec: ExecutionContext): Future[HttpResponse] = {
    val executingRequest = addHeaders(req)
    logger.debug(s"Req: => $executingRequest")
    Http().singleRequest(executingRequest).andThen({
      case Success(resp) => logger.debug(s"Resp: => ${resp}")
      case Failure(exception) => logger.error(s"Failed request with ${exception.getMessage}",exception)
    })
  }
}
