@regression
Feature: API gateway technical contract

  Background:
    * url baseUrl

  Scenario: ping and native gateway endpoints are exposed
    Given path 'api', 'system', 'ping'
    When method get
    Then status 200
    And match response.service == 'api-gateway'
    And match response.status == 'UP'

    Given path 'actuator', 'gateway', 'routes'
    When method get
    Then status 200
