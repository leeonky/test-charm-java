Feature: Replace

  Background:
    Given base url "http://www.a.com"

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
      | PATCH  |
