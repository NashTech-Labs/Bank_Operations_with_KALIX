# Bank Operations with KALIX

---

This project is a demonstration of Event Sourcing with KALIX.</br>
In this project we have two Services by which we can perform some banking operations like:</br>
1. [ Create an account. ](#create)
2. [ Credit an account. ](#credit)
3. [ Debit an account.  ](#debit)
4. [ Get details of an account.  ](#getDetails)

## Services:
There are two service in this project:</br>
1. BankOperationsMVC
2. BankOperationsService 

Currently, we are using `BankOperationsMVC` to create an account, because as we know that for ES the entiyID
plays a very important role. With the help of the entityID we can traverse the entities as well as their events.
So, when we talk about Event Sourcing in this project. the account will be an entity and  account number will be the 
entitID. Now we can't generate the account number on our own. So, we are using `BankOperationsMVC` as controller that will generate 
the account number and will call the main create method of `BankOperationsService`.
</br>
All other operations will be available directly in the `BankOperationsService` service.

<a name="create"></a>
## 1. Create an account.

To create an account you have to call the `createAccountRequest` method of `BankOperationsMVC` service from the gRPCUI or you 
can hit the endpoint with the required request json shown as below:</br></br>
endpoint -> `http://localhost:8080/account/creationRequest` </br>
methodType -> POST</br>
request  -> 
```
{
  "uid": "9876543211234",
  "name": "Yash Gupta",
  "address": "RandomAdress",
  "city": "RandomCity",
  "state": "RandomState"
}
```

As response you will get your account number
```
{
    "accNo": "6ff06a58-6328-4070-803c-9825b3258200"
}
```

<a name="credit"></a>
## 2. Credit an account.

To credit an account you have to call the `creditAccount` method of `BankOperationsService` service from the gRPCUI or you
can hit the endpoint with the required request json shown as below:</br></br>
endpoint -> `http://localhost:8080/account/credit` </br>
methodType -> POST</br>
request  ->
```
{
  "accNo": "6ff06a58-6328-4070-803c-9825b3258200",
  "recipientName": "Rudra",
  "amount": 500
}
```

As response you will get:
```
{
    "accNo": "6ff06a58-6328-4070-803c-9825b3258200",
    "operationType": "CREDITED",
    "amount": 500.0
}
```


<a name="debit"></a>
## 3. Debit an account.

To debit an account you have to call the `debitAccount` method of `BankOperationsService` service from the gRPCUI or you
can hit the endpoint with the required request json shown as below:</br></br>
endpoint -> `http://localhost:8080/account/debit` </br>
methodType -> POST</br>
request  ->
```
{
  "accNo": "6ff06a58-6328-4070-803c-9825b3258200",
  "recipientName": "Amit",
  "amount": 400
}
```

As response you will get:
```
{
    "accNo": "6ff06a58-6328-4070-803c-9825b3258200",
    "operationType": "DEBITED",
    "amount": 400.0
}
```


<a name="getDetails"></a>
## 4. Get details of an account.

To get the details of an account you have to call the `getAccountInformation` method of `BankOperationsService` service from the gRPCUI, 
you will get full details of the particular account number. Or you
can hit the endpoint with the certain account number:</br></br>
endpoint -> `http://localhost:8080/account/{accNo}` </br>
methodType -> GET</br>

example -> `http://localhost:8080/account/6ff06a58-6328-4070-803c-9825b3258200`

As response you will get whole account information with all the transaction:
```
{
    "accNo": "6ff06a58-6328-4070-803c-9825b3258200",
    "totalAmount": 700.0,
    "accountDetails": {
        "uid": "9876543231234",
        "name": "Yash Gupta",
        "address": "RandomAdress",
        "city": "RandomCity",
        "state": "RandomState",
        "createdDtm": "1657793090157"
    },
    "transactions": [
        {
            "id": "9078caee-4a10-421d-91af-2a7406ceb568",
            "recipientName": "BANK",
            "operation": "JOINING_BONUS",
            "amount": 100.0,
            "createdDtm": "1657793090157",
            "totalAmount": 100.0
        },
        {
            "id": "281804d5-f5a8-43e0-990a-d2dcaa56f467",
            "recipientName": "Rudra",
            "operation": "CREDITED",
            "amount": 500.0,
            "createdDtm": "1657793498593",
            "totalAmount": 600.0
        },
        {
            "id": "294b3730-12b7-4a97-9a30-777533cc214b",
            "recipientName": "Rudra",
            "amount": 500.0,
            "createdDtm": 1.65779351306E12,
            "totalAmount": 1100.0
        },
        {
            "id": "bbe087c6-85fd-4ebe-b176-1178f5e36680",
            "recipientName": "Amit",
            "operation": "DEBITED",
            "amount": 400.0,
            "createdDtm": "1657793676730",
            "totalAmount": 700.0
        }
    ]
}
```

You can also querry specific fields like current balance, user details, and can generate the statement.

### Get current balance

endpoint -> `http://localhost:8080/account/{accNo}/balance` </br>
example -> `http://localhost:8080/account/6ff06a58-6328-4070-803c-9825b3258200/balance` </br>
As response you will get the current balance:
```
700.0
```

### Get account details

endpoint -> `http://localhost:8080/account/{accNo}/details` </br>
example -> `http://localhost:8080/account/6ff06a58-6328-4070-803c-9825b3258200/details` </br>
As response you will get the details of the given account number:
```
{
    "uid": "9876543231234",
    "name": "Yash Gupta",
    "address": "RandomAdress",
    "city": "RandomCity",
    "state": "RandomState",
    "createdDtm": "1657793090157"
}
```

### Get account statement

endpoint -> `http://localhost:8080/account/{accNo}/statement` </br>
example -> `http://localhost:8080/account/6ff06a58-6328-4070-803c-9825b3258200/statement` </br>
As response you will get all transactions as a list:
```
[
    {
        "id": "9078caee-4a10-421d-91af-2a7406ceb568",
        "recipientName": "BANK",
        "operation": "JOINING_BONUS",
        "amount": 100.0,
        "createdDtm": 1.657793090157E12,
        "totalAmount": 100.0
    },
    {
        "id": "281804d5-f5a8-43e0-990a-d2dcaa56f467",
        "recipientName": "Rudra",
        "amount": 500.0,
        "createdDtm": 1.657793498593E12,
        "totalAmount": 600.0
    },
    {
        "id": "294b3730-12b7-4a97-9a30-777533cc214b",
        "recipientName": "Rudra",
        "amount": 500.0,
        "createdDtm": 1.65779351306E12,
        "totalAmount": 1100.0
    },
    {
        "id": "bbe087c6-85fd-4ebe-b176-1178f5e36680",
        "recipientName": "Amit",
        "operation": "DEBITED",
        "amount": 400.0,
        "createdDtm": 1.65779367673E12,
        "totalAmount": 700.0
    }
]
```

## Unit Testing

Currently, we are only focusing on the Unit testing.
</br>There are two classes that contains unit test cases:
1. BankOperationsMVCServiceActionSpec (Contains unit test cases for cross-component calls)
2. BankOperationsSpec (Contains unit test cases for the API)

By default, the integration and unit test are both invoked by the `sbt test`.
but here, we are only focusing on unit test cases,
So to run only unit tests, run the `sbt -DonlyUnitTest test`, or `sbt -DonlyUnitTest=true test`, or set up that value to `true` in the sbt session by `set onlyUnitTest := true` and then run `test`.


## Package and deploy this project


To build and publish the container image and then deploy the service, follow these steps:

1. Use the `Docker/publish` task to build the container image and publish it to your container registry. At the end of this command sbt will show you the container image URL you’ll need in the next part of this process.
```
    sbt Docker/publish -Ddocker.username=[your-docker-hub-username]`
```

2. If you haven’t done so yet, sign in to your Kalix account. 
If this is your first time using Kalix, this will let you register an account,
[create your first project](https://docs.kalix.io/projects/create-project.html), and set this project as the default.
```aidl
    kalix auth login
```

3. [Deploy the service](https://docs.kalix.io/services/deploy-service.html#_deploy) with the published container image from above:
```aidl
    kalix service deploy <service name> <container image>
```
4. You can [verify the status of the deployed](https://docs.kalix.io/services/deploy-service.html#_verify_service_status) service using:
```aidl
    kalix service list
```

## Invoke your service

Once the service has started successfully, you can [start a proxy locally](https://docs.kalix.io/services/invoke-service.html#_testing_and_development) to access the service:
```aidl
    kalix service proxy <service name> --grpcui
```

The --grpcui option also starts and opens a gRPC web UI for exploring and invoking the service (available at http://127.0.0.1:8080/ui/).

Or you can use postman and other option to hit the above-mentioned endpoints with correspondence requests.