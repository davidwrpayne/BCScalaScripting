package scripts.api.model

import scripts.api.BigcommerceApi
import spray.json.{JsNumber, JsObject, JsString}

case class OrderProduct(name: String, quantity: Int, priceExcludingTax: BigDecimal = 0, priceIncludingTax: BigDecimal= 0 , sku: String = "", upc: String = "") {
  def getJsonRepresentation(): JsObject = {
    JsObject(
      "name" -> JsString(name),
      "quantity" -> JsNumber(quantity),
      "price_inc_tax" -> JsNumber(priceIncludingTax),
      "price_ex_tax" -> JsNumber(priceExcludingTax)
    )
  }
}
