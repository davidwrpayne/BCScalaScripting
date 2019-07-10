package scripts.api.model

import spray.json.{JsNumber, JsObject, JsString, JsValue}
import spray.json.lenses.{Reader, Validated}

case class Customer(id: Option[Int], email: String, firstName: String, lastName: String) {
}

object Customer
 {
   val reader = new Reader[Customer] {
     override def read(js: JsValue): Validated[Customer] = {
       js match {
         case obj: JsObject =>
           obj.getFields("id","email","first_name","last_name") match {
             case Seq(JsNumber(jId),JsString(jEmail),JsString(jFirstName),JsString(jLastName)) =>
               Right(Customer(
                 id = Some(jId.toInt),
                 email = jEmail,
                 firstName = jFirstName,
                 lastName =  jLastName
               ))
             case x =>
               Left(InvalidModelJsonException(s"Unable to deserialize $x"))
           }
         case unexpected => Left(InvalidModelJsonException(s"Expected JsObject. received: $unexpected"))
       }
     }
   }
 }
