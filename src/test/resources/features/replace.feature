Feature: Replace

  Background:
    Given base url "http://www.a.com"

  Scenario: Not replace
    When GET "/${NotExist}"
    Then "http://www.a.com" got a "GET" request on "/$%7BNotExist%7D"
