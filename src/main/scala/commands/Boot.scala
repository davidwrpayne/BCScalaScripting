package commands

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.scalalogging.LazyLogging
import scripts.ApiScripting
import scala.concurrent.duration._
import scripts.api.{AkkaHttpClient, BigcommerceApi}

import scala.concurrent.{Await, ExecutionContext}

object Boot extends App with LazyLogging with CommandLinePrompts {

  implicit val system: ActorSystem = ActorSystem("ScriptingSystem")
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val mat: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))


  val defaultApiUrl = "https://api.bigcommerce.com"
  val AuthClientIdHeader = "X-Auth-Client"
  val AuthTokenHeader = "X-Auth-Token"


  val lastArgs: LastPromptResponses = readLastArguments()
  val promptResponse = promptForInput(lastArgs)
  writeLastArguments(promptResponse)

  val accessToken = promptResponse.token
  val clientId = promptResponse.id
  val storeHash = promptResponse.storeHash
  val AuthHeaders: Seq[HttpHeader] = scala.collection.immutable.Seq(RawHeader(AuthClientIdHeader, clientId), RawHeader(AuthTokenHeader, accessToken))
  val client = AkkaHttpClient(AuthHeaders)
  val bcApi = BigcommerceApi(promptResponse.apiUrl, storeHash, client)



  val scripting = new ApiScripting(bcApi, 300)
  scripting.executeBlogPosts(promptResponse)
  Await.ready(system.terminate(), 10 seconds)


}
