@regression
Feature: Maintenance contract

  Background:
    * url baseUrl

  Scenario: maintenance CRUD flow returns the managed contract
    * def carId = uuid()
    Given path 'api', 'maintenances'
    And request { information: 'Brake pads replaced', carId: '#(carId)' }
    When method post
    Then status 201
    And match response.carId == carId
    And match response.information == 'Brake pads replaced'
    And match response.id == '#string'
    * def maintenanceId = response.id

    Given path 'api', 'maintenances', maintenanceId
    When method get
    Then status 200
    And match response.id == maintenanceId
    And match response.carId == carId

    Given path 'api', 'maintenances'
    When method get
    Then status 200
    And match response.content[*].id contains maintenanceId

    Given path 'api', 'maintenances', maintenanceId
    When method delete
    Then status 204

  Scenario: creating a maintenance validates the request body
    * def carId = uuid()
    Given path 'api', 'maintenances'
    And request { information: '', carId: '#(carId)' }
    When method post
    Then status 400
    And match response.type == 'VALIDATION_EXCEPTION'
    And match response.details.information == 'Maintenance information is required.'

  Scenario: maintenance reports invalid identifiers and missing resources
    Given path 'api', 'maintenances', 'not-a-uuid'
    When method get
    Then status 400
    And match response.type == 'REQUEST_FORMAT_EXCEPTION'
    And match response.details[0] contains 'UUID'

    * def missingId = uuid()
    Given path 'api', 'maintenances', missingId
    When method get
    Then status 422
    And match response.type == 'BUSINESS_EXCEPTION'
    And match response.message == 'Messages.Maintenance.NotExists'
