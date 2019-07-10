package scripts.api

import akka.http.scaladsl.model.Uri

trait Api {
  def client: HttpClient

  /**
    * Append Path is path with slash to be appended.
    * @param appendPath
    * @return
    */
  def baseUrl(appendPath: Option[String] = None): Uri

}
