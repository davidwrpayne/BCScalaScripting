package scripts.api

import akka.stream.Materializer
import scripts.api.model.Pagination
import spray.json.DefaultJsonProtocol._
import spray.json._
import spray.json.lenses._
import scripts.api.model.Product

import scala.concurrent.{ExecutionContext, Future}

trait ProductsApi {
  this: BigcommerceApi =>
  val productsPath = "/v3/catalog/products"

  /**
    * Extraction lenses for json data extraction
    */

  import spray.json.lenses.JsonLenses._

  private val IdLens: Lens[Seq] = "data" / * / "id"
  private val productsLens: Lens[Seq] = "data" / *
  private val paginationLens: ScalarLens = "meta" / "pagination"

  def getProductPage(page: Int): Future[String] = {
    getApiPage(productsPath, Some(page))
  }

  def getProductResponse()(implicit ec: ExecutionContext, mat: Materializer): Future[String] = {
    getProductPage(1)
  }
  /**
    * Fetches first page gets count of product pages and parallel fetches them and combines with products returned from current page
    * @param ec
    * @param mat
    * @return
    */
  def getAllProductIds()(implicit ec: ExecutionContext): Future[Seq[Int]] = {
    for {
      pageJson <- getApiPage(productsPath, None)
      page = pageJson.parseJson
      pagination = page.extract[Pagination](paginationLens)(Pagination.reader)
      productIds = page.extract[Int](IdLens)
      pagesToFetch = if(pagination.currentPage != pagination.totalPages) pagination.currentPage + 1 to pagination.totalPages  else Seq.empty[Int]
      x: Seq[Future[Seq[Int]]] = pagesToFetch.map(getProductPage(_).map(_.parseJson.extract[Int](IdLens)))
      y <- Future.sequence(x).map(_.flatten)
    } yield {
      y ++ productIds
    }
  }

  def getAllProducts()(implicit ec: ExecutionContext): Future[Seq[Product]] = {
    for {
      pageJson <- getApiPage(productsPath, None)
      page = pageJson.parseJson
      pagination = page.extract[Pagination](paginationLens)(Pagination.reader)
      productIds = page.extract[Product](productsLens)(Product.reader)
      pagesToFetch = if(pagination.currentPage != pagination.totalPages) pagination.currentPage + 1 to pagination.totalPages  else Seq.empty[Int]
      x = pagesToFetch.map(getProductPage(_).map(_.parseJson.extract[Product](productsLens)(Product.reader)))
      y <- Future.sequence(x).map(_.flatten)
    } yield {
      y ++ productIds
    }
  }



  def getProductMetaData()(implicit ec: ExecutionContext): Future[String] = {
    for {
      body <- getProductPage(0)
      json = body.parseJson
      pagination = json.extract[Pagination](paginationLens)(Pagination.reader)
    } yield {
      pagination.toString
    }
  }

}
