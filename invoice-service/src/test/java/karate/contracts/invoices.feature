@regression
Feature: Invoice contract

  Background:
    * url baseUrl

  Scenario: listing invoices returns the seeded contract
    Given path 'api', 'invoices'
    When method get
    Then status 200
    And match response[*].id contains seedId
    And match response[*].plate contains seedPlate

  Scenario: invoice OpenAPI docs are exposed
    Given path 'v3', 'api-docs'
    When method get
    Then status 200
    And match response.paths['/api/invoices'] != null
