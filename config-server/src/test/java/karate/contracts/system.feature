@regression
Feature: Config server technical contract

  Background:
    * url baseUrl

  Scenario: ping and native actuator endpoints are exposed
    Given path 'api', 'system', 'ping'
    When method get
    Then status 200
    And match response.service == 'config-server'
    And match response.status == 'UP'

    Given path 'actuator', 'env'
    When method get
    Then status 200
