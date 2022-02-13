Feature: Replace

  Background:
    Given base url "http://www.a.com"

  Scenario: Not replace
    When GET "/${NotExist}"
    Then "http://www.a.com" got a "GET" request on "/$%7BNotExist%7D"

  Scenario Outline: Replace in path for <method>
    Given var "pathVariable" value is "replacedPath"
    When <method> "/${pathVariable}"
    Then "http://www.a.com" got a "<method>" request on "/replacedPath"
    Examples:
      | method |
      | GET    |
      | DELETE |

  Scenario Outline: Replace in path and body for <method>
    Given var "pathVariable" value is "replacedPath"
    Given var "bodyVariable" value is "replacedBody"
    When <method> "/${pathVariable}":
    """
    ${bodyVariable}
    """
    Then "http://www.a.com" got a "<method>" request on "/replacedPath" with body
    """
    replacedBody
    """
    Examples:
      | method |
      | POST   |
      | PUT    |

