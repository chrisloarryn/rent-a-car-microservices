@regression
Feature: Discovery server technical contract

  Background:
    * url baseUrl

  Scenario: ping and health endpoints are exposed
    Given path 'api', 'system', 'ping'
    When method get
    Then status 200
    And match response.service == 'discovery-server'
    And match response.status == 'UP'

    Given path 'actuator', 'health'
    When method get
    Then status 200
    And match response.status == 'UP'
