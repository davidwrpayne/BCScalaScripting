package scripts

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import scripts.api.{AkkaHttpClient, BigcommerceApi}

import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps

object Boot extends App {

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
  val ids = Await.result(bcApi.getAllProductIds(), 300 seconds)
  println(s" size: ${ids.size}")
  println(s" ids: $ids")

  val result = system.terminate()
  println("finished exiting")
}
