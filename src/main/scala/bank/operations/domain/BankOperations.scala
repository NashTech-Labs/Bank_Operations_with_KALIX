package bank.operations.domain

import bank.operations.{api, domain}
import bank.operations.api.{AccountCreationResponse, AccountInfo, AccountTransactionStatus}
import bank.operations.api.AccountCreationResponse.IsAccountCreated.{ACCOUNT_EXIST, CREATION_FAILED, CREATION_SUCCEED}
import bank.operations.domain.ShapelessObjects.{genAccountDetailsApi, genAccountDetailsDomain}
import bank.operations.domain.Transaction.OperationType
import kalix.scalasdk.eventsourcedentity.EventSourcedEntity
import kalix.scalasdk.eventsourcedentity.EventSourcedEntityContext

import java.util.UUID

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class BankOperations(context: EventSourcedEntityContext) extends AbstractBankOperations {
  override def emptyState: AccountState =
    AccountState.defaultInstance

  override def createAccount(currentState: AccountState, account: api.Account): EventSourcedEntity.Effect[api.AccountCreationResponse] = {
    if(currentState.accNo.nonEmpty){
      val response = AccountCreationResponse(
        isAccountCreated = ACCOUNT_EXIST,
        reply = "This Account is already exist, please generate new one!"
      )
      effects.reply(response)
    }else{
      val accountChecker = account.uid.split('|').toSeq
      if(accountChecker.contains("fromMVC") && accountChecker.size > 1){
        val event = AccountCreated(
          account.accNo,
          accountChecker.head,
          account.name,
          account.address,
          account.city,
          account.state,
          account.creationDate
        )
        val response = AccountCreationResponse(
          isAccountCreated = CREATION_SUCCEED,
          reply = "Congratulations! Your account is created successfully and joining bonus is added by Bank."
        )
        effects.emitEvent(event)
          .thenReply(_ => response)
      }else{
        val response = AccountCreationResponse(
          isAccountCreated = CREATION_FAILED,
          reply = "Please request the new account creation from MVC, and provide correct information"
        )
        effects.reply(response)
      }
    }
  }

  override def creditAccount(currentState: AccountState, accountCreditRequest: api.AccountCreditRequest): EventSourcedEntity.Effect[api.AccountTransactionStatus] = {
    if(!currentState.accNo.equals(accountCreditRequest.accNo) || currentState.accNo.isEmpty){
      effects.error(s"Sorry, Account number: ${accountCreditRequest.accNo} does not exist!")
    }else{
      val transactionId: String = UUID.randomUUID().toString
      val transactionDate: Long = System.currentTimeMillis()
      val event = AccountCredited(
        transactionId = transactionId,
        recipientName = accountCreditRequest.recipientName,
        amount = accountCreditRequest.amount,
        createdDtm = transactionDate
      )
      val response = AccountTransactionStatus(
        accNo = accountCreditRequest.accNo,
        operationType = api.OperationType.CREDITED,
        amount = accountCreditRequest.amount
      )
      effects.emitEvent(event)
        .thenReply(_ => response)
    }
  }

  override def debitAccount(currentState: AccountState, accountDebitRequest: api.AccountDebitRequest): EventSourcedEntity.Effect[api.AccountTransactionStatus] = {
    if(!currentState.accNo.equals(accountDebitRequest.accNo) || currentState.accNo.isEmpty){
      effects.error(s"Sorry, Account number: ${accountDebitRequest.accNo} does not exist")
    }else{
      val transactionId: String = UUID.randomUUID().toString
      val transactionDate: Long = System.currentTimeMillis()
      if(currentState.totalAmount >= accountDebitRequest.amount){
        val event = AccountDebited(
          transactionId = transactionId,
          recipientName = accountDebitRequest.recipientName,
          amount = accountDebitRequest.amount,
          createdDtm = transactionDate
        )
        val response = AccountTransactionStatus(
          accNo = accountDebitRequest.accNo,
          operationType = api.OperationType.DEBITED,
          amount = accountDebitRequest.amount
        )
        effects.emitEvent(event)
          .thenReply(_ => response)
      }else{
        effects.error("Insufficient balance.")
      }
    }
  }

  override def getAccountInformation(currentState: AccountState, accountInformationRequest: api.AccountInformationRequest): EventSourcedEntity.Effect[api.AccountInfo] = {
    if(currentState.accNo.isEmpty){
      effects.error(s"Sorry, ${accountInformationRequest.accNo} is not valid")
    }else{
      val transactions =
        currentState.transactions
          .map{
            x =>
              api.Transaction(
                id = x.id,
                recipientName = x.recipientName,
                operation = api.OperationType.fromValue(x.operation.value),
                amount = x.amount,
                createdDtm = x.createdDtm,
                totalAmount = x.totalAmount
              )
          }

      val accountDetailsOption = currentState.accountDetails match {
        case Some(value) =>
          Some(convertDomainToApi(value))
        case _ => None
      }

      val accountInfo = AccountInfo(
        accNo = currentState.accNo,
        totalAmount = currentState.totalAmount,
        accountDetails = accountDetailsOption,
        transactions = transactions
      )
      effects.reply(accountInfo)
    }
  }

  override def accountCreated(currentState: AccountState, accountCreated: AccountCreated): AccountState = {
    AccountState(
      accountCreated.accNo,
      100.0,
      Some(domain.AccountDetails(
        uid = accountCreated.uid,
        name = accountCreated.name,
        address = accountCreated.address,
        city = accountCreated.city,
        state = accountCreated.state,
        createdDtm = accountCreated.createdDtm
      )),
      Seq(
        domain.Transaction(
          id = UUID.randomUUID().toString,
          recipientName = "BANK",
          operation = OperationType.JOINING_BONUS,
          amount = 100.0,
          createdDtm = accountCreated.createdDtm,
          totalAmount = 100.0
        )
      )
    )
  }

  override def accountCredited(currentState: AccountState, accountCredited: AccountCredited): AccountState = {
    val newTransaction = domain.Transaction(
      id = accountCredited.transactionId,
      recipientName = accountCredited.recipientName,
      operation = OperationType.CREDITED,
      amount = accountCredited.amount,
      createdDtm = accountCredited.createdDtm,
      totalAmount = currentState.totalAmount + accountCredited.amount
    )
    currentState.copy(
      transactions = currentState.transactions :+ newTransaction,
      totalAmount = currentState.totalAmount+accountCredited.amount
    )
  }

  override def accountDebited(currentState: AccountState, accountDebited: AccountDebited): AccountState = {
    val newTransaction = domain.Transaction(
      id = accountDebited.transactionId,
      recipientName = accountDebited.recipientName,
      operation = OperationType.DEBITED,
      amount = accountDebited.amount,
      createdDtm = accountDebited.createdDtm,
      totalAmount = currentState.totalAmount - accountDebited.amount
    )
    currentState.copy(
      transactions = currentState.transactions :+ newTransaction,
      totalAmount = currentState.totalAmount - accountDebited.amount
    )
  }

  private def convertDomainToApi(request: domain.AccountDetails): api.AccountDetails = {
    genAccountDetailsApi.from(
      genAccountDetailsDomain.to(
        request
      )
    )
  }
}
