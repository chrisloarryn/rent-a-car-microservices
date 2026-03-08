@regression
Feature: Rental contract

  Background:
    * url baseUrl

  Scenario: rental CRUD flow returns the managed contract
    * def carId = uuid()
    Given path 'api', 'rentals'
    And request
    """
    {
      "carId":"#(carId)",
      "dailyPrice":120.0,
      "rentedForDays":3,
      "cardNumber":"1234567812345678",
      "cardHolder":"John Doe",
      "cardExpirationYear":2027,
      "cardExpirationMonth":12,
      "cardCvv":"123"
    }
    """
    When method post
    Then status 201
    And match response.carId == carId
    And match response.totalPrice == 360.0
    And match response.id == '#string'
    * def rentalId = response.id

    Given path 'api', 'rentals', rentalId
    When method get
    Then status 200
    And match response.id == rentalId

    Given path 'api', 'rentals'
    When method get
    Then status 200
    And match response[*].id contains rentalId

    Given path 'api', 'rentals', rentalId
    When method delete
    Then status 204

  Scenario: creating a rental validates the request body
    Given path 'api', 'rentals'
    And request {}
    When method post
    Then status 400
    And match response.type == 'VALIDATION_EXCEPTION'
    And match response.details.carId == 'Car id is required.'

  Scenario: rental reports invalid identifiers and missing resources
    Given path 'api', 'rentals', 'not-a-uuid'
    When method get
    Then status 400
    And match response.type == 'REQUEST_FORMAT_EXCEPTION'
    And match response.details[0] contains 'UUID'

    * def missingId = uuid()
    Given path 'api', 'rentals', missingId
    When method get
    Then status 422
    And match response.type == 'BUSINESS_EXCEPTION'
    And match response.message == 'RENTAL_NOT_EXISTS'
