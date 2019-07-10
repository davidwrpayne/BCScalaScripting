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


  def getProductResponse()(implicit ec: ExecutionContext, mat: Materializer): Future[String] = {
    getJsonResponse(productsPath)
  }


  /**
    * Extraction lenses for json data extraction
    */

  import spray.json.lenses.JsonLenses._

  private val IdLens: Lens[Seq] = "data" / * / "id"
  private val paginationLens: ScalarLens = "meta" / "pagination"

  private def getJsonResponse(path: String)(implicit ec: ExecutionContext, mat: Materializer): Future[String] = {
    val req = HttpRequest(HttpMethods.GET, baseUrl(Some(productsPath)))
    client.executeRequest(req)
      .flatMap(materializeEntity)
      .map(_._2.parseJson)
      .map(_.prettyPrint)
  }


  def getProductPage(page: Int)(implicit ec: ExecutionContext, mat: Materializer): Future[String] = {
    val query = Uri.Query(("page", page.toString))
    for {
      body <- client.executeRequest(HttpRequest(uri = baseUrl(Some(productsPath)).withQuery(query)))
      stringBody <- materializeEntity(body)
    } yield {
      stringBody._2
    }
  }

  /**
    * Fetches first page gets count of product pages and parallel fetches them and combines with products returned from current page
    * @param ec
    * @param mat
    * @return
    */
  def getAllProductIds()(implicit ec: ExecutionContext, mat: Materializer): Future[Seq[Int]] = {
    for {
      pageJson <- getProductPage(0)
      page = pageJson.parseJson
      pagination = page.extract[Pagination](paginationLens)(Pagination.reader)
      productIds = page.extract[Int](IdLens)
      pagesToFetch = pagination.currentPage + 1 to pagination.totalPages
      x: Seq[Future[Seq[Int]]] = pagesToFetch.map(getProductPage(_).map(_.parseJson.extract[Int](IdLens)))
      y <- Future.sequence(x).map(_.flatten)
    } yield {
      y ++ productIds
    }
  }


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
