package scripts

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.scalalogging.LazyLogging
import commands.Boot.{AuthClientIdHeader, AuthTokenHeader, logger}
import commands.PromptResponse
import org.slf4j.Logger
import scripts.api.{AkkaHttpClient, BigcommerceApi, model}
import scripts.api.model.{Customer, OrderProduct}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random
import scala.concurrent.duration._

class ApiScripting(bcApi: BigcommerceApi, timeoutSeconds: Long)
                  (implicit ac: ActorSystem, ec: ExecutionContext, mat: ActorMaterializer)
  extends LazyLogging {

  /**
    * Unfortunately this only creates orders 1 at a time. I've run into issues with kicking off all the requests
    * as akka seems to have a default limit of 32 outbound requests? Need to kick off the requests in batches probably
    * @param bcApi
    * @param customers
    * @param products
    * @return
    */
  private def createOrders(bcApi: BigcommerceApi, customers: Seq[Customer], products: Seq[model.Product]): Future[Seq[Option[Int]]] = {
    val percentOfCustomers: Double = .80
    val percentOfProducts: Double = .8
    val percentOfGuestOrders: Double = .10
    // create random number of orders per guest
    val numberOfGuestOrders: Double = Math.ceil(percentOfGuestOrders * customers.size)
    val customerWithOrders: Seq[Customer] = Random.shuffle(customers).take(Math.ceil(customers.size * percentOfCustomers).toInt)
    val productsToCreateOrdersFrom: Seq[model.Product] = Random.shuffle(products).take(Math.ceil(products.size * percentOfProducts).toInt)

    val customerProductList = customerWithOrders.flatMap({ customer =>
      val products = Random.shuffle(productsToCreateOrdersFrom).take(Math.ceil(Random.nextFloat() * 3).toInt + 1)
      products.map((customer, _))
    })

    customerProductList.foldLeft(Future.successful(Seq.empty[Option[Int]]))({
      case (prev: Future[Seq[Option[Int]]], (customer, product)) =>
        prev.flatMap({ case res: Seq[Option[Int]] =>
          bcApi.createOrder(Some(customer), OrderProduct(product.name, 1, product.price, product.price + product.price * .07, product.sku, "")).map(Some(_)).recoverWith({
            case e: Exception =>
              logger.info(s"failed to create an order for customer: $customer and product $product with exception ${e.getMessage}")
              Future.successful(None)
          }).map((x: Option[Int]) => res ++ Seq(x))
        })

    })
  }


  def runStoreScript(promptResponse: PromptResponse) = {

    val accessToken = promptResponse.token
    val clientId = promptResponse.id
    val storeHash = promptResponse.storeHash

    val AuthHeaders: Seq[HttpHeader] = scala.collection.immutable.Seq(RawHeader(AuthClientIdHeader, clientId), RawHeader(AuthTokenHeader, accessToken))
    val client = AkkaHttpClient(AuthHeaders)

    val bcApi = BigcommerceApi(promptResponse.apiUrl, storeHash, client)

    var customers: Seq[Customer] = Seq.empty[Customer]
    var products: Seq[model.Product] = Seq.empty[model.Product]

    /** For scripting purposes I'll await results but should try and find a better way to handle this **/
    customers = Await.result(bcApi.getAllCustomers(), 300 seconds)
    products = Await.result(bcApi.getAllProducts(), 300 seconds)


    if (promptResponse.createOrders) {
      val result = createOrders(bcApi, customers, products)
      val orderIdsCreated = Await.result(result, timeoutSeconds seconds)
      println(s" created orders $orderIdsCreated")
    }

    Await.result(bcApi.getAllOrderJson(), timeoutSeconds seconds)

  }

  def executeBlogPosts(promptResponse: PromptResponse): Future[Unit] = {
    val accessToken = promptResponse.token
    val clientId = promptResponse.id
    val storeHash = promptResponse.storeHash
    val AuthHeaders: Seq[HttpHeader] = scala.collection.immutable.Seq(RawHeader(AuthClientIdHeader, clientId), RawHeader(AuthTokenHeader, accessToken))
    val client = AkkaHttpClient(AuthHeaders)

    val bcApi = BigcommerceApi(promptResponse.apiUrl, storeHash, client)

    var blogPosts = bcApi.getAllBlogPosts()
    Future.successful(())
  }
}
