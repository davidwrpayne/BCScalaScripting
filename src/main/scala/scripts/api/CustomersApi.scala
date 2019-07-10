package scripts.api

import scripts.api.model.{Customer, Pagination}
import spray.json.lenses._
import spray.json._
import DefaultJsonProtocol._

import scala.concurrent.{ExecutionContext, Future}

trait CustomersApi {
  this: BigcommerceApi =>

  val customerPath = "/v3/customers"

  def getCustomersPage(i: Int): Future[String] = {
    getApiPage(customerPath,Some(i))
  }
  import spray.json.lenses.JsonLenses._
  private val IdLens: Lens[Seq] = "data" / * / "id"
  private val paginationLens: ScalarLens = "meta" / "pagination"
  private val customerLens: Lens[Seq] = "data" / *
  /**
    * Fetches first page gets count of product pages and parallel fetches them and combines with products returned from current page
    *
    * @param ec
    * @return
    */
  def getAllCustomerIds()(implicit ec: ExecutionContext): Future[Seq[Int]] = {
    for {
      pageJson <- getApiPage(customerPath,None)
      page = pageJson.parseJson
      pagination = page.extract[Pagination](paginationLens)(Pagination.reader)
      customerIdsFetchedFromFirstPage = page.extract[Int](IdLens)
      remainderOfPagesToFetch = if(pagination.currentPage != pagination.totalPages) pagination.currentPage to pagination.totalPages else Seq.empty[Int]
      x: Seq[Future[Seq[Int]]] = remainderOfPagesToFetch.map(getProductPage(_).map(_.parseJson.extract[Int](IdLens)))
      y <- Future.sequence(x).map(_.flatten)
    } yield {
      y ++ customerIdsFetchedFromFirstPage
    }
  }

  def getCustomerJson(customerId: Int)(implicit ec: ExecutionContext): Future[JsObject] = {
    for {
      customerPage <- getApiPage(s"$customerPath/$customerId", None)
    } yield {
      customerPage.parseJson.asJsObject()
    }
  }

  def getAllCustomers()(implicit ec: ExecutionContext): Future[Seq[Customer]] = {
    for {
      pageJson <- getApiPage(customerPath,None)
      page = pageJson.parseJson
      pagination = page.extract[Pagination](paginationLens)(Pagination.reader)
      customersFetchedFromFirstPage = page.extract[Customer](customerLens)(Customer.reader)
      remainderOfPagesToFetch = if(pagination.currentPage != pagination.totalPages) pagination.currentPage + 1 to pagination.totalPages  else Seq.empty[Int]
      x = remainderOfPagesToFetch.map(getCustomersPage(_).map(_.parseJson.extract[Customer](customerLens)(Customer.reader)))
      y <- Future.sequence(x).map(_.flatten)
    } yield {
      y ++ customersFetchedFromFirstPage
    }
  }
}
