package scripts.api.model

import faker.Base
import spray.json.{JsObject, JsString, JsValue}

import scala.util.Random

case class BillingAddress(
                           firstName: String,
                           lastName: String,
                           company: String,
                           street1: String,
                           street2: String,
                           city: String,
                           state: String,
                           zip: String,
                           country: String,
                           countryISO2: String,
                           phone: String,
                           email: String
                         ) {
  def getJsonRep(): JsValue = {
    JsObject(
      "first_name" -> JsString(firstName),
      "last_name" -> JsString(lastName),
      "company" -> JsString(company),
      "street_1" -> JsString(street1),
      "street_2" -> JsString(street2),
      "city" -> JsString(city),
      "state" -> JsString(state),
      "zip" -> JsString(zip),
      "country" -> JsString(country),
      "country_iso2" -> JsString(countryISO2),
      "phone" -> JsString(phone),
      "email" -> JsString(email)
    )
  }


}


object BillingAddress extends Base {

  import faker._
  def getFakeAddress(): String = {
    Seq(
      numerify(fetch[String]("address.street_address")),
      fetch[String]("name.first_name"),
      fetch[String]("address.street_suffix")
    ).mkString(" ")
  }
  def getFakeCity(): String = {
    Seq(
      numerify(fetch[String]("address.city_prefix")),
      fetch[String]("name.first_name").concat(fetch[String]("address.city_suffix"))
    ).mkString(" ")
  }

  def getNextFakeAddress(): BillingAddress = {
    Faker.locale("en")
    val name = Name.name
    BillingAddress(
      Name.first_name,
      Name.last_name,
      Company.name,
      getFakeAddress(),
      "",
      getFakeCity(),
      fetch[String]("address.state"),
      numerify(fetch[String]("address.postcode")),
      "United States",
      "US",
      PhoneNumber.phone_number,
      Internet.email(name)
    )
  }
}
