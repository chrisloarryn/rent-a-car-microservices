@regression
Feature: Payment contract

  Background:
    * url baseUrl

  Scenario: payment CRUD and rental processing return managed contracts
    * def createRequest =
    """
    {
      "cardNumber": "1234567812345678",
      "cardHolder": "John Doe",
      "cardExpirationYear": 2027,
      "cardExpirationMonth": 12,
      "cardCvv": "123",
      "balance": 500.0
    }
    """
    Given path 'api', 'payments'
    And request createRequest
    When method post
    Then status 201
    And match response.cardNumber == createRequest.cardNumber
    And match response.id == '#string'
    * def paymentId = response.id

    Given path 'api', 'payments', paymentId
    When method get
    Then status 200
    And match response.id == paymentId

    Given path 'api', 'payments', 'process-rental-payment'
    And request
    """
    {
      "cardNumber": "1234567812345678",
      "cardHolder": "John Doe",
      "cardExpirationYear": 2027,
      "cardExpirationMonth": 12,
      "cardCvv": "123",
      "price": 100.0
    }
    """
    When method post
    Then status 200
    And match response.success == true

    Given path 'api', 'payments', paymentId
    When method delete
    Then status 204

  Scenario: creating a payment validates the request body
    Given path 'api', 'payments'
    And request {}
    When method post
    Then status 400
    And match response.type == 'VALIDATION_EXCEPTION'
    And match response.details.cardNumber == 'Card number cant be empty...'

  Scenario: payment reports invalid identifiers and missing resources
    Given path 'api', 'payments', 'not-a-uuid'
    When method get
    Then status 400
    And match response.type == 'REQUEST_FORMAT_EXCEPTION'
    And match response.details[0] contains 'UUID'

    * def missingId = uuid()
    Given path 'api', 'payments', missingId
    When method get
    Then status 422
    And match response.type == 'BUSINESS_EXCEPTION'
    And match response.message == 'PAYMENT_NOT_FOUND'
