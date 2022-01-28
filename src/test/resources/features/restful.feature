Feature: RESTful api steps

  Scenario: get with no params
    Given base url "http://www.a.com"
    When GET "/index"
    Then "http://www.a.com" got a GET request on "/index"