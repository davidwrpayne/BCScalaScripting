package scripts.api.model

import spray.json.{JsNumber, JsObject, JsString, JsValue}
import spray.json.lenses.{Reader, Validated}

case class Product(id: Int, name: String, price: BigDecimal, sku: String){



}



object Product {

  val reader = new Reader[Product] {
    override def read(js: JsValue): Validated[Product] = {
      js match {
        case obj: JsObject =>
          obj.getFields("id","name","price","sku") match {
            case Seq(JsNumber(id),JsString(name),JsNumber(price),JsString(sku)) =>
              Right(Product(id.toInt,name,price.bigDecimal,sku))
            case x =>
              Left(InvalidModelJsonException(s"Unable to deserialize $x"))
          }
        case unexpected => Left(InvalidModelJsonException(s"Expected JsObject. received: $unexpected"))
      }
    }
  }
}