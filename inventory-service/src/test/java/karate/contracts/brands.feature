@regression
Feature: Inventory brand contract

  Background:
    * url baseUrl

  Scenario: brand CRUD flow returns the managed contract
    Given path 'api', 'brands'
    And request { name: 'Audi' }
    When method post
    Then status 201
    And match response.name == 'Audi'
    And match response.id == '#string'
    * def brandId = response.id

    Given path 'api', 'brands', brandId
    When method get
    Then status 200
    And match response == { id: '#(brandId)', name: 'Audi' }

    Given path 'api', 'brands'
    When method get
    Then status 200
    And match response[*].id contains brandId

    Given path 'api', 'brands', brandId
    When method delete
    Then status 204

  Scenario: creating a brand validates the request body
    Given path 'api', 'brands'
    And request {}
    When method post
    Then status 400
    And match response.type == 'VALIDATION_EXCEPTION'
    And match response.path == '/api/brands'
    And match response.details.name == 'Brand name is required.'

  Scenario: inventory reports invalid identifier formats and missing resources
    Given path 'api', 'brands', 'not-a-uuid'
    When method get
    Then status 400
    And match response.type == 'REQUEST_FORMAT_EXCEPTION'
    And match response.details[0] contains 'UUID'

    * def missingId = uuid()
    Given path 'api', 'brands', missingId
    When method get
    Then status 422
    And match response.type == 'BUSINESS_EXCEPTION'
    And match response.message == 'BRAND_NOT_EXISTS'
