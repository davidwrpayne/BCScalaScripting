package scripts.api

import akka.http.scaladsl.model.{HttpResponse, Uri}
import akka.stream.Materializer
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}

trait Api {
  def client: HttpClient

  /**
    * Append Path is path with slash to be appended.
    * @param appendPath
    * @return
    */
  def baseUrl(appendPath: Option[String] = None): Uri


  def materializeEntity(resp: HttpResponse)(implicit ec: ExecutionContext, mat: Materializer): Future[(HttpResponse, String)] = {
    for {
      body <- resp.entity.dataBytes.runFold(ByteString(""))(_ ++ _)
    } yield {
      (resp, body.utf8String)
    }
  }
}
