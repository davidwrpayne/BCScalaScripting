package commands

case class PromptResponse(token: String, id: String, storeHash: String, apiUrl: String, createOrders: Boolean)

case class LastPromptResponses(
                                token: Option[String] = None,
                                id: Option[String] = None,
                                storeHash: Option[String] = None,
                                apiUrl: Option[String] = None,
                                createOrdersArguments: Option[String] = None
                              )