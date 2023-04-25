@jfactory
Feature: JFactory Integration

  Background:
    Given base url "http://www.a.com"

  Scenario Outline: <method> with body created by spec
    When <method> "LoginRequest" "/index":
    """
    {
      "username": "admin",
      "captcha": {
        "code": "1234"
      }
    }
    """
    Then "http://www.a.com" got a "<method>" request on "/index" with body matching
    """
    : [{
      method: '<method>'
      path: '/index'
      headers: {
        ['Content-Type']: ['application/json']
      }
      body.json= {
        username: admin,
        password: password#1,
        captcha: {
          code: '1234'
        }
      }
    }]
    """
    Examples:
      | method |
      | POST   |
      | PUT    |

  Scenario Outline: <method> with body array created by spec
    When <method> "LoginRequest" "/index":
    """
    [{
      "username": "admin",
      "captcha": {
        "code": "1234"
      }
    }]
    """
    Then "http://www.a.com" got a "<method>" request on "/index" with body matching
    """
    : [{
      body.json= [{
        username: admin,
        password: password#1,
        captcha: {
          code: '1234'
        }
      }]
    }]
    """
    Examples:
      | method |
      | POST   |
      | PUT    |

  Scenario Outline: <method> with body created by trait and spec
    When <method> "<traitAndSpec>" "/index":
    """
    {
      "username": "admin"
    }
    """
    Then "http://www.a.com" got a "<method>" request on "/index" with body matching
    """
    : [{
      body.json= {
        username: admin,
        password: wrongPassword,
        captcha: null
      }
    }]
    """
    Examples:
      | method | traitAndSpec               |
      | POST   | WrongPassword LoginRequest |
      | POST   | WrongPassword,LoginRequest |
      | PUT    | WrongPassword LoginRequest |
      | PUT    | WrongPassword,LoginRequest |


  Scenario Outline: <method> with body array created by trait and spec
    When <method> "<traitAndSpec>" "/index":
    """
    [{
      "username": "admin"
    }]
    """
    Then "http://www.a.com" got a "<method>" request on "/index" with body matching
    """
    : [{
      body.json= [{
        username: admin,
        password: wrongPassword,
        captcha: null
      }]
    }]
    """
    Examples:
      | method | traitAndSpec               |
      | POST   | WrongPassword LoginRequest |
      | POST   | WrongPassword,LoginRequest |
      | PUT    | WrongPassword LoginRequest |
      | PUT    | WrongPassword,LoginRequest |

  Scenario Outline: <method> with body created by spec and inline replace
    Given var "user" value is "admin"
    When <method> "LoginRequest" "/index":
    """
    {
      "username": "${user}"
    }
    """
    Then "http://www.a.com" got a "<method>" request on "/index" with body matching
    """
    : [{
      body.json.username= admin
    }]
    """
    Examples:
      | method |
      | POST   |
      | PUT    |
