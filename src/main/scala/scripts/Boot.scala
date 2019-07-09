package scripts

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import scripts.api.BigcommerceApi

import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps

object Boot extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("OrderCreatorActorSystem")
  implicit val ec: ExecutionContext = actorSystem.dispatcher
  implicit val mat: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(actorSystem))


  val apiUrl = "https://api.bigcommerce.com"

  val accessToken = ""
  val clientId = ""
  val clientSecret = ""
  val storeHash = ""

  val client = new BigcommerceApi(apiUrl, storeHash, clientId, accessToken)

  import scala.concurrent.duration._

  println(Await.result(client.getAllProducts(), 10 seconds))


  actorSystem.terminate()
}
