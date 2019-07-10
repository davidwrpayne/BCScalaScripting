package scripts.api

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

case class BigcommerceApi(apiBaseUrl: String, storeHash: String, client: HttpClient)
                    (implicit ec: ExecutionContext, sys: ActorSystem, mat: ActorMaterializer)
  extends Api
    with ProductsApi
    with LazyLogging {

  override def baseUrl(appendPath: Option[String] = None): Uri = {
    appendPath match {
      case None => Uri(apiBaseUrl).withPath(Path(s"/stores/$storeHash"))
      case Some(appendablePath) => Uri(apiBaseUrl).withPath(Path(s"/stores/$storeHash$appendablePath"))
    }
  }

}
