package commands

import java.io.{File, PrintWriter}

import scala.io.Source

trait CommandLinePrompts {
  this: Boot.type =>
  val FILE_LOCATION = "./.argument-config"

  /** * ##############################  Main above, Funcs below  ############################## ***/

  def writeLastArguments(arguments: PromptResponse, fileLocation: String = FILE_LOCATION): Unit = {
    val f = new File(fileLocation)
    f.delete()
    val writer = new PrintWriter(f)
    val lines = Seq(
      ("create_orders", arguments.createOrders),
      ("api_url", arguments.apiUrl),
      ("store_hash", arguments.storeHash),
      ("access_token", arguments.token),
      ("client_id", arguments.id)
    )
    lines
      .map({ case (key, value) => (key, value.toString) })
      .filter({ case (_, value) => value.nonEmpty })
      .foreach(line => writer.write(s"${line._1}=${line._2}\n"))
    writer.close()
  }

  def readLastArguments(fileLocation: String = FILE_LOCATION): LastPromptResponses = {
    if (scala.reflect.io.File(fileLocation).exists) {
      val source = Source.fromFile(fileLocation)
      val lines = for {
        line <- source.getLines().toSeq
        splits = line.split('=')
      } yield {
        (splits.headOption, splits.lastOption)
      }
      val definedLines: Map[String, Option[String]] = lines.collect({ case (Some(key), Some(value)) => (key, Some(value)) }).toMap
      source.close()
      LastPromptResponses(
        token = definedLines.getOrElse[Option[String]]("access_token", None),
        id = definedLines.getOrElse[Option[String]]("client_id", None),
        storeHash = definedLines.getOrElse[Option[String]]("store_hash", None),
        apiUrl = definedLines.getOrElse[Option[String]]("api_url", None),
        createOrdersArguments = definedLines.getOrElse[Option[String]]("create_orders", None)
      )
    } else {
      LastPromptResponses()
    }
  }


  private def promptUser(prompt: String, defaultValue: Option[String]): String = {
    defaultValue match {
      case Some(value) =>
        val input = scala.io.StdIn.readLine(String.format(prompt, value))
        if (input.trim.isEmpty) value else input.trim
      case None => scala.io.StdIn.readLine(String.format(prompt,""))
    }
  }

  def promptForInput(possiblePromptResponse: LastPromptResponses): PromptResponse = {
    PromptResponse(
      apiUrl = promptUser("Enter Base Api Url [%s]:", Some(possiblePromptResponse.apiUrl.getOrElse(defaultApiUrl))),
      storeHash = promptUser("Enter StoreHash [%s]:", possiblePromptResponse.storeHash),
      token = promptUser("Enter Access Token [%s]:", possiblePromptResponse.token),
      id = promptUser("Enter Client Id [%s]:", possiblePromptResponse.id),
      createOrders = promptUser("create Orders [%s]:", Some(possiblePromptResponse.createOrdersArguments.getOrElse("false"))).trim.toLowerCase == "true"
    )
  }
}
