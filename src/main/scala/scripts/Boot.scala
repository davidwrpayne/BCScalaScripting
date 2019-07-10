package scripts

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.scalalogging.LazyLogging
import scripts.api.model.{BillingAddress, Customer, OrderProduct}
import scripts.api.{AkkaHttpClient, BigcommerceApi, model}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Random, Success}

object Boot extends App with LazyLogging {

  implicit val system: ActorSystem = ActorSystem("OrderCreatorActorSystem")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val mat: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))


  val apiUrl = "https://api.bigcommerce.com"

  val accessToken = ""
  val clientId = ""
  val clientSecret = ""
  val storeHash = ""

  val AuthClientIdHeader = "X-Auth-Client"
  val AuthTokenHeader = "X-Auth-Token"
  val AuthHeaders: Seq[HttpHeader] = scala.collection.immutable.Seq(RawHeader(AuthClientIdHeader, clientId), RawHeader(AuthTokenHeader, accessToken))
  val client = AkkaHttpClient(AuthHeaders)

  val bcApi = BigcommerceApi(apiUrl, storeHash, client)

  import scala.concurrent.duration._

  //  val orderProduct = OrderProduct("someProd",1,3.0,4.0)
  val customers: Seq[Customer] = Await.result(bcApi.getAllCustomers(), 300 seconds)
  val products = Await.result(bcApi.getAllProducts(), 300 seconds)

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

  val result = customerProductList.foldLeft(Future.successful(Seq.empty[Option[Int]]))({
    case (prev: Future[Seq[Option[Int]]], (customer, product)) =>
      prev.flatMap({ case res: Seq[Option[Int]] =>
        bcApi.createOrder(Some(customer), OrderProduct(product.name, 1, product.price, product.price + product.price * .07, product.sku, "")).map(Some(_)).recoverWith({
          case e: Exception =>
            logger.info(s"failed to create an order for customer: $customer and product $product with exception ${e.getMessage}")
            Future.successful(None)
        }).map((x: Option[Int]) => res ++ Seq(x))
      })

  })

  val orderIds = Await.result(result, 300 seconds)
  println(s" created orders $orderIds")


  Await.ready(system.terminate(), 10 seconds)
}
