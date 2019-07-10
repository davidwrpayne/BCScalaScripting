package scripts.api

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

case class BigcommerceApi(apiBaseUrl: String, storeHash: String, client: HttpClient)
                         (implicit ec: ExecutionContext, sys: ActorSystem, mat: ActorMaterializer)
  extends LazyLogging
    with ProductsApi
    with CustomersV3Api {

  /**
    * Append Path is path with slash to be appended.
    *
    * @param appendPath
    * @return
    */
  def baseUrl(appendPath: Option[String] = None): Uri = {
    appendPath match {
      case None => Uri(apiBaseUrl).withPath(Path(s"/stores/$storeHash"))
      case Some(appendablePath) => Uri(apiBaseUrl).withPath(Path(s"/stores/$storeHash$appendablePath"))
    }
  }

  private def materializeEntity(resp: HttpResponse): Future[(HttpResponse, String)] = {
    for {
      body <- resp.entity.dataBytes.runFold(ByteString(""))(_ ++ _)
    } yield {
      (resp, body.utf8String)
    }
  }

  def getApiPage(path: String, page: Option[Int]): Future[String] = {
    val uri = baseUrl(Some(path))
    val modURI = page match {
      case Some(page) =>
        uri.withQuery(Uri.Query(("page", page.toString)))
      case None =>
        uri
    }
    val request = HttpRequest(uri = modURI)
    for {
      body <- client.executeRequest(request)
      stringBody <- materializeEntity(body)
    } yield {
      stringBody._2
    }
  }

}
