package bank.operations.domain

import bank.operations.api
import bank.operations.api.AccountInformationRequest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class BankOperationsSpec extends AnyWordSpec with Matchers {
  "The BankOperations" should {

    val accountNumber = UUID.randomUUID().toString
    val testKit = BankOperationsTestKit(new BankOperations(_))

    "correctly process commands of type createAccount" in {

      val account = api.Account(
        accNo = accountNumber,
        uid = "1234567895" + "|" + "fromMVC",
        name = "Yash Gupta",
        address = "RandomAddress",
        city = "RandomCity",
        state = "RandomState",
        creationDate = System.currentTimeMillis()
      )

      val result = testKit.createAccount(account)

      result.events should have size 1

      val accountCreated = result.nextEvent[AccountCreated]

      accountCreated.accNo shouldBe accountNumber
    }

    "correctly process commands of type creditAccount" in {

      val accountCreditedRequest = api.AccountCreditRequest(
        accNo = accountNumber,
        recipientName = "Rudra Gupta",
        amount = 500.0
      )

      val result = testKit.creditAccount(accountCreditedRequest)

      result.events should have size 1

      val accountCredited = result.nextEvent[AccountCredited]

      accountCredited.amount shouldBe 500.0
      accountCredited.recipientName shouldBe "Rudra Gupta"
    }

    "correctly process commands of type debitAccount" in {

      val accountDebitedRequest = api.AccountDebitRequest(
        accNo = accountNumber,
        recipientName = "Nitesh Kumar",
        amount = 400.0
      )

      val result = testKit.debitAccount(accountDebitedRequest)

      result.events should have size 1

      val accountCredited = result.nextEvent[AccountDebited]

      accountCredited.amount shouldBe 400.0
      accountCredited.recipientName shouldBe "Nitesh Kumar"
    }

    "correctly process commands of type getAccountInformation" in {

      val getAccountInformationRequest = AccountInformationRequest(
        accNo = accountNumber
      )

      val result = testKit.getAccountInformation(getAccountInformationRequest)

      val accountDetails = result.reply

      accountDetails.accNo shouldBe accountNumber
      accountDetails.transactions.size shouldBe 3
    }
  }
}
