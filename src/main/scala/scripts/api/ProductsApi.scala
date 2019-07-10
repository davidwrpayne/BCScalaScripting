package scripts.api

import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, Uri}
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.ByteString
import scripts.api.model.Pagination
import spray.json.{JsArray, JsObject, JsValue}
import spray.json.lenses._
import spray.json.DefaultJsonProtocol._

import scala.concurrent.{ExecutionContext, Future}
import spray.json._

trait ProductsApi {
  this: Api =>

  val productsPath = "/v3/catalog/products"


  def materializeEntity(resp: HttpResponse)(implicit ec: ExecutionContext, mat: Materializer): Future[(HttpResponse, String)] = {
    for {
      body <- resp.entity.dataBytes.runFold(ByteString(""))(_ ++ _)
    } yield {
      (resp, body.utf8String)
    }
  }


  def getProductResponse()(implicit ec: ExecutionContext, mat: Materializer): Future[String] = {
    getJsonResponse(productsPath)
  }


  /**
    * Extraction lenses for json data extraction
    */

  import spray.json.lenses.JsonLenses._

  private val MetaLens: ScalarLens = strToField("meta")
  private val IdLens: Lens[Seq] = "data" / * / "id"
  private val totalPagesLens: Lens[Id] = "meta" / "pagination" / "total_pages"
  private val paginationLens: ScalarLens = "meta" / "pagination"

  def getProducts[T](transform: JsValue => Seq[T])
                    (implicit ec: ExecutionContext, mat: Materializer): (Seq[T], JsObject) = {
    //    client.executeRequest()
    ???
  }

  private def getJsonResponse(path: String)(implicit ec: ExecutionContext, mat: Materializer): Future[String] = {
    val req = HttpRequest(HttpMethods.GET, baseUrl(Some(productsPath)))
    client.executeRequest(req)
      .flatMap(materializeEntity)
      .map(_._2.parseJson)
      .map(_.prettyPrint)
  }

//
//  def getAllProductIds()(implicit ec: ExecutionContext, mat: Materializer): Future[Seq[Int]] = {
//    val req = HttpRequest(HttpMethods.GET, baseUrl(Some(productsPath)))
//    for {
//      (_, body) <- client.executeRequest(req).flatMap(materializeEntity)
//      s = body.parseJson
//    } yield {
//      s.asJsObject().getFields("data").headOption match {
//        case Some(value: JsArray) =>
//          value.elements.flatMap(_.asJsObject.getFields("id").map(_.convertTo[Int]))
//        case _ => Seq.empty[Int]
//      }
//    }
//  }


  def getProductMetaData()(implicit ec: ExecutionContext, mat: Materializer): Future[String] = {
    for {
      body <- getJsonResponse(productsPath)
      json = body.parseJson
      pagination = json.extract[Pagination](paginationLens)(Pagination.reader)
    } yield {
      pagination.toString
    }
  }

}
