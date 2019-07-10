package scripts.api.model

import spray.json.{JsObject, JsString, JsValue}

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


object BillingAddress {

  import faker._

  def getNextFakeAddress(): BillingAddress = {
    Faker.locale("en")
    val name = Name.name
    BillingAddress(
      Name.first_name,
      Name.last_name,
      Company.name,
      "",
      "",
      "oakland",
      "california",
      "94608",
      "United States",
      "US",
      PhoneNumber.phone_number,
      Internet.email(name)
    )
  }
}
