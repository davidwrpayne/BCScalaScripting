package scripts.api

import scala.concurrent.Future
import scripts.api.model.{BillingAddress, Customer, OrderProduct, Pagination}
import spray.json.lenses._
import spray.json._
import DefaultJsonProtocol._

import scala.concurrent.{ExecutionContext, Future}
import spray.json.lenses.JsonLenses._

import scala.util.Random


trait OrdersApi {
  this: BigcommerceApi =>
  val ordersApiPath = "/v2/orders"

  val idLens = strToField("id")
  private val paginationLens = "meta" / "pagination"
  //  def updateOrderStatus(orderId: Int, status: Int):Future[Unit] = {
  //    put(s"$ordersApiPath/$orderId",update)
  //  }

  /**
    * returns id of order created
    *
    * @return
    */
  def createOrder(customerDetails: Option[Customer], orderProducts: OrderProduct)(implicit ec: ExecutionContext): Future[Int] = {

    val newOrder = buildOrder(customerDetails, Seq(orderProducts))

    for {
      orderResponse <- executePost(ordersApiPath, newOrder.toString)
      orderId = orderResponse.extract[Int](idLens)
      //      _ <- updateOrderStatus(orderId, Random.nextInt(14)+1)
    } yield {
      orderId
    }
  }

  private def buildBillingAddress(customerDetails: Option[Customer]): JsValue = {
    BillingAddress.getNextFakeAddress().getJsonRep()
  }

  private def buildOrder(customerDetails: Option[Customer], product: Seq[OrderProduct]): JsObject = {
    val defaultContent: Seq[JsField] = List(
      ("status_id", JsNumber(Random.nextInt(14) + 1)),
      ("products", JsArray(product.map(_.getJsonRepresentation()).toVector))
    )
    val fieldsToAdd: Seq[JsField] = customerDetails match {
      case Some(Customer(None, _, _, _)) =>
        throw new Exception("provide a customer with an id if your trying to attach an order to a customer")
      case Some(Customer(Some(id), _, _, _)) =>
        List(
          ("customer_id", JsNumber(id)),
          ("billing_address", buildBillingAddress(customerDetails))
        )
      case None =>
        List(("billing_address", buildBillingAddress(None)))
    }
    JsObject(defaultContent ++ fieldsToAdd: _*)
  }


  def getAllOrderJson()(implicit ec: ExecutionContext): Future[Seq[JsObject]] = {
      for {
        orderBody <- getApiPage(ordersApiPath,None)
        json = orderBody
        _ = println(orderBody)
//        jsonPagination = pri
//        _ = println(jsonPagination)
//        pagination = json.extract[Pagination](paginationLens)(Pagination.reader)
      } yield {
        Seq.empty
      }
  }
}
