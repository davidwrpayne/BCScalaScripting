package scripts

import java.io.{File, PrintWriter}

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
import scala.concurrent.duration._
import scala.io.Source

object Boot extends App with LazyLogging {

  implicit val system: ActorSystem = ActorSystem("OrderCreatorActorSystem")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val mat: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))
  

  val defaultApiUrl = "https://api.bigcommerce.com"

  val AuthClientIdHeader = "X-Auth-Client"
  val AuthTokenHeader = "X-Auth-Token"
  val FILE_LOCATION = "./.argument-config"



  val lastArgs: PossiblePromptResponse = readLastArguments()
  val arguments = promptForInput(lastArgs)
  writeLastArguments(arguments)
  runStoreScript(arguments)
  Await.ready(system.terminate(), 10 seconds)

  /*** ##############################  Main above, Funcs below  ############################## ***/


  def writeLastArguments(arguments: PromptResponse, fileLocation: String = FILE_LOCATION): Unit = {
    val f = new File(fileLocation)
    f.delete()
    val writer = new PrintWriter(f)
    val lines = Seq(
      ("create_orders",arguments.createOrders),
      ("api_url",arguments.apiUrl),
      ("store_hash", arguments.storeHash),
      ("access_token", arguments.token),
      ("client_id", arguments.id)
    )
    lines
      .map({case (key,value) => (key,value.toString)})
      .filter({case (_,value) => value.nonEmpty})
      .foreach(line => writer.write(s"${line._1}=${line._2}\n"))
    writer.close()
  }

  def readLastArguments(fileLocation: String = FILE_LOCATION): PossiblePromptResponse = {
    if (scala.reflect.io.File(fileLocation).exists ) {
      val source = Source.fromFile(fileLocation)
      val lines = for {
        line <- source.getLines().toSeq
        splits = line.split('=')
      } yield { (splits.headOption,splits.lastOption) }
      val definedLines: Map[String, Option[String]] = lines.collect({case (Some(key),Some(value)) => (key,Some(value))}).toMap
      source.close()
      PossiblePromptResponse(
        token = definedLines.getOrElse[Option[String]]("access_token", None),
        id = definedLines.getOrElse[Option[String]]("client_id", None),
        storeHash = definedLines.getOrElse[Option[String]]("store_hash", None),
        apiUrl = definedLines.getOrElse[Option[String]]("api_url", None),
        createOrdersArguments = definedLines.getOrElse[Option[String]]("create_orders", None)
      )
    } else {
      PossiblePromptResponse()
    }
  }


  case class PromptResponse(token: String, id: String, storeHash: String, apiUrl: String, createOrders: Boolean)

  case class PossiblePromptResponse(
                                     token: Option[String] = None,
                                     id: Option[String] = None,
                                     storeHash: Option[String] = None,
                                     apiUrl: Option[String] = None,
                                     createOrdersArguments: Option[String] = None
                                   )

  private def promptUser(prompt: String, defaultValue: Option[String]): String = {
    defaultValue match {
      case Some(value) =>
        val input = scala.io.StdIn.readLine(String.format(prompt, value))
        if (input.trim.isEmpty) value else input.trim
      case None => scala.io.StdIn.readLine(String.format(prompt,""))
    }
  }

  private def promptForInput(possiblePromptResponse: PossiblePromptResponse): PromptResponse = {
    PromptResponse(
      apiUrl = promptUser("Enter Api Url [%s]:", Some(possiblePromptResponse.apiUrl.getOrElse(defaultApiUrl))),
      token = promptUser("Enter Access Token [%s]:", possiblePromptResponse.token),
      storeHash = promptUser("Enter StoreHash [%s]:", possiblePromptResponse.storeHash),
      id = promptUser("Enter Client Id [%s]:", possiblePromptResponse.id),
      createOrders = promptUser("create Orders [%s]:", Some(possiblePromptResponse.createOrdersArguments.getOrElse("false"))).trim.toLowerCase == "true"
    )
  }

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


  private def runStoreScript(promptResponse: PromptResponse) = {

    val accessToken = promptResponse.token
    val clientId = promptResponse.id
    val storeHash = promptResponse.storeHash

    val AuthHeaders: Seq[HttpHeader] = scala.collection.immutable.Seq(RawHeader(AuthClientIdHeader, clientId), RawHeader(AuthTokenHeader, accessToken))
    val client = AkkaHttpClient(AuthHeaders)

    val bcApi = BigcommerceApi(promptResponse.apiUrl, storeHash, client)

    var customers: Seq[Customer] = Seq.empty[Customer]
    var products: Seq[model.Product] = Seq.empty[model.Product]
    customers = Await.result(bcApi.getAllCustomers(), 300 seconds)
    products = Await.result(bcApi.getAllProducts(), 300 seconds)


    if (promptResponse.createOrders) {
      val result = createOrders(bcApi, customers, products)
      val orderIdsCreated = Await.result(result, 300 seconds)
      println(s" created orders $orderIdsCreated")
    }

    Await.result(bcApi.getAllOrderJson(), 300 seconds)

  }
}
