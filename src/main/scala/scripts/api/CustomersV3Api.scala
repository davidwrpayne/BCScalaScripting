package scripts.api

import scripts.api.model.Pagination
import spray.json.lenses._
import spray.json._
import DefaultJsonProtocol._
import scala.concurrent.{ExecutionContext, Future}

trait CustomersV3Api {
  this: BigcommerceApi =>

  val customerPath = "/v3/customers"

  def getCustomersPage(i: Int): Future[String] = {
    getApiPage(customerPath,Some(i))
  }
  import spray.json.lenses.JsonLenses._
  private val IdLens: Lens[Seq] = "data" / * / "id"
  private val paginationLens: ScalarLens = "meta" / "pagination"
  /**
    * Fetches first page gets count of product pages and parallel fetches them and combines with products returned from current page
    *
    * @param ec
    * @return
    */
  def getAllCustomerIds()(implicit ec: ExecutionContext): Future[Seq[Int]] = {
    for {
      pageJson <- getApiPage(customerPath,None)
      _ = println("customer response: " + pageJson.toString)
      page = pageJson.parseJson
      pagination = page.extract[Pagination](paginationLens)(Pagination.reader)
      customerIds = page.extract[Int](IdLens)
      pagesToFetch = if(pagination.currentPage != pagination.totalPages) pagination.currentPage to pagination.totalPages else Seq.empty[Int]
      x: Seq[Future[Seq[Int]]] = pagesToFetch.map(getProductPage(_).map(_.parseJson.extract[Int](IdLens)))
      y <- Future.sequence(x).map(_.flatten)
    } yield {
      y ++ customerIds
    }
  }
}
