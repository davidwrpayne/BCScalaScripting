package scripts.api

import akka.actor.{ActorLogging, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse, Uri}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import spray.json._
import spray.json.DefaultJsonProtocol._

import scala.concurrent.{ExecutionContext, Future}

class BigcommerceApi(apiBaseUrl: String, storeHash: String, clientId: String, token: String)
                    (implicit ec: ExecutionContext, sys: ActorSystem, mat: ActorMaterializer)
  extends LazyLogging {
  val productsPath = "/v3/catalog/products"
  val AuthClientIdHeader = "X-Auth-Client"
  val AuthTokenHeader = "X-Auth-Token"
  val AuthHeaders: Seq[HttpHeader] = scala.collection.immutable.Seq(RawHeader(AuthClientIdHeader, clientId), RawHeader(AuthTokenHeader, token))

  private def addHeaders(req: HttpRequest): HttpRequest = {
    AuthHeaders.foldLeft(req)((req, h) => req.addHeader(h))
  }

  def getApiUri(path: String): Uri = {
    Uri(apiBaseUrl).withPath(Path(s"/stores/$storeHash$path"))
  }

  def getAllProducts(): Future[Seq[Int]] = {
    val req = HttpRequest(uri = getApiUri(productsPath))
    for {
      httpResponse <- executeRequest(addHeaders(req))
      x <- httpResponse.entity.dataBytes.runFold(ByteString(""))(_ ++ _)
    } yield {
      val s = x.utf8String.parseJson
      s.asJsObject().getFields("data").headOption match {
        case Some(value: JsArray) =>
          value.elements.flatMap(_.asJsObject.getFields("id").map(_.convertTo[Int]))
        case _ => Seq.empty[Int]
      }
    }
  }

  def getAllCustomers(): Future[Seq[Int]] = {
    ???
  }


  def executeRequest(req: HttpRequest): Future[HttpResponse] = {
    logger.debug(s"Req: => $req")
    Http().singleRequest(addHeaders(req))
  }
}
