@regression
Feature: Filter contract

  Background:
    * url baseUrl

  Scenario: listing and fetching filters returns the seeded contract
    Given path 'api', 'filters'
    When method get
    Then status 200
    And match response.content[*].id contains seedId

    Given path 'api', 'filters', seedId
    When method get
    Then status 200
    And match response.id == seedId
    And match response.plate == seedPlate

  Scenario: filter endpoints report invalid identifiers and missing resources
    Given path 'api', 'filters', 'not-a-uuid'
    When method get
    Then status 400
    And match response.type == 'REQUEST_FORMAT_EXCEPTION'
    And match response.details[0] contains 'UUID'

    * def missingId = uuid()
    Given path 'api', 'filters', missingId
    When method get
    Then status 422
    And match response.type == 'BUSINESS_EXCEPTION'
    And match response.message == 'FILTER_NOT_EXISTS'
