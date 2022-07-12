package bank.operations.domain

import bank.operations.{api, domain}
import shapeless._

object ShapelessObjects {

  val genAccountDetailsDomain = Generic[domain.AccountDetails]
  val genAccountDetailsApi = Generic[api.AccountDetails]

  val genTransactionDomain = Generic[domain.Transaction]
  val genTransactionApi = Generic[api.Transaction]
}
