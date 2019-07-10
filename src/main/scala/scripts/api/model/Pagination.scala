package scripts.api.model

import spray.json.lenses._
import spray.json.{JsNumber, JsObject, JsValue}

case class Pagination(count: Int, perPage: Int, currentPage: Int, total: Int, totalPages: Int)
object Pagination {
  val reader = new Reader[Pagination] {
    override def read(js: JsValue): Validated[Pagination] = {
        js match {
          case obj: JsObject =>
            obj.getFields("count","per_page","current_page","total_pages","total") match {
              case Seq(JsNumber(jCount),JsNumber(jPerPage),JsNumber(jCurrentPage),JsNumber(jTotalPages),JsNumber(jTotal)) =>
                Right(Pagination(
                  count = jCount.toInt,
                  perPage = jPerPage.toInt,
                  currentPage = jCurrentPage.toInt,
                  totalPages = jTotalPages.toInt,
                  total = jTotal.toInt
                ))
              case x =>
                Left(InvalidModelJsonException(s"Unable to deserialize $x"))
            }
          case unexpected => Left(InvalidModelJsonException(s"Expected JsObject. recieved: $unexpected"))
        }
    }
  }
}