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

  Scenario: Replace in upload file request
    Given var "fileKeyVar" value is "an avatar"
    Given var "keyVar" value is "name"
    Given var "pathVar" value is "users"
    Given a file "an avatar":
    """
    hello avatar
    """
    When POST form "/${pathVar}":
    """
    {
      "${keyVar}": "Tom",
      "@avatar": "${fileKeyVar}"
    }
    """
    And got request form value:
    """
    : [{
      headers: /.*name="name"(.|\r|\n)*/
      body.string: 'Tom'
    } {
      headers: /.*name="avatar"(.|\r|\n)*/
      headers: /.*filename=".*\.upload"(.|\r|\n)*/
      body.string: 'hello avatar'
    }]
    """
