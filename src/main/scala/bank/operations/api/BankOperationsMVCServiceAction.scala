package bank.operations.api

import bank.operations.api.AccountCreationResponse.IsAccountCreated.{ACCOUNT_EXIST, CREATION_FAILED, CREATION_SUCCEED}
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext

import java.util.UUID
import scala.concurrent.Future

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class BankOperationsMVCServiceAction(creationContext: ActionCreationContext) extends AbstractBankOperationsMVCServiceAction {

  override def createAccountRequest(accountCreationRequest: AccountCreationRequest): Action.Effect[AccountNo] = {
    if(accountCreationRequest.uid.isEmpty){
      effects.error(s"Sorry ${accountCreationRequest.name}, UID can't be empty")
    }else{
      val request = accountCreationRequest
      val accountNo = UUID.randomUUID().toString
      val tamperedUID = request.uid + "|" + "fromMVC"

      val account = Account(
        accNo = accountNo,
        uid = tamperedUID,
        name = request.name,
        address = request.address,
        city = request.city,
        state = request.state,
        creationDate = System.currentTimeMillis()
      )
      val created: Future[AccountCreationResponse] = components.bankOperations.createAccount(account).execute()

      val effect: Future[Action.Effect[AccountNo]] =
        created.map{
          response =>
            response.isAccountCreated match {
              case CREATION_FAILED =>
                effects.error(s"Sorry ${accountCreationRequest.name}, your account is not created, ${response.reply}")
              case ACCOUNT_EXIST =>
                effects.error(s"Sorry ${accountCreationRequest.name}, your account is not created, Please try Again.")
              case CREATION_SUCCEED =>
                effects.reply(AccountNo(accNo = accountNo))
              case _ =>
                effects.error(s"Sorry wrong input")
            }
        }.recover(_ => effects.error("Failed to create Account, please retry"))

      effects.asyncEffect(effect)
    }
  }
}

