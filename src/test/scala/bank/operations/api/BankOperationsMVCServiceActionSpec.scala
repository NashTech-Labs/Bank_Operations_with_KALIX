package bank.operations.api

import bank.operations.api.AccountCreationResponse.IsAccountCreated
import kalix.scalasdk.testkit.MockRegistry
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class BankOperationsMVCServiceActionSpec
    extends AnyWordSpec
    with Matchers
    with MockFactory{

  "BankOperationsMVCServiceAction" should {

    "create an account and return an account number" in {

      val mockBankOperationsService = stub[BankOperationsService]

      val accountCreationResponse = AccountCreationResponse(
        isAccountCreated = IsAccountCreated.CREATION_SUCCEED,
        reply = "Congratulations! Account is created and joining bonus is added to your account"
      )

      (mockBankOperationsService.createAccount _)
        .when(*)
        .returns(Future.successful(accountCreationResponse))

      val mockRegistry = MockRegistry.withMock(mockBankOperationsService)

      val service = BankOperationsMVCServiceActionTestKit(new BankOperationsMVCServiceAction(_), mockRegistry)

      val accountCreationRequest = AccountCreationRequest(
        uid = "1234567890",
        name = "Yash Gupta",
        address = "RandomAddress",
        city = "RandomCity",
        state = "RandomState"
      )

      val result = service.createAccountRequest(accountCreationRequest).asyncResult

      result.map{
        res =>
          res.reply.accNo.isEmpty shouldBe false
      }
    }

    "not create an account, throw an error due to an empty UID" in {

      val mockBankOperationsService = stub[BankOperationsService]

      (mockBankOperationsService.createAccount _)
        .when(*)
        .returns(Future.successful(AccountCreationResponse.defaultInstance))

      val mockRegistry = MockRegistry.withMock(mockBankOperationsService)

      val service = BankOperationsMVCServiceActionTestKit(new BankOperationsMVCServiceAction(_), mockRegistry)

      val accountCreationRequest = AccountCreationRequest(
        uid = "",
        name = "Yash Gupta",
        address = "RandomAddress",
        city = "RandomCity",
        state = "RandomState"
      )

      val result = service.createAccountRequest(accountCreationRequest).isError

      result shouldBe true
    }

  }
}
