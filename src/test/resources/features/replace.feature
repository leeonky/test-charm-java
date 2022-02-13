Feature: Replace

  Background:
    Given base url "http://www.a.com"

  Scenario: Not replace
    Given no replacement
    When GET "/${NotExist}"
    Then "http://www.a.com" got a "GET" request on "/$%7BNotExist%7D"

  Scenario: Replace in path
    Given var "pathVariable" value is "replacedPath"
    When GET "/${pathVariable}"
    Then "http://www.a.com" got a "GET" request on "/replacedPath"

