@jfactory
Feature: JFactory Integration

  Background:
    Given base url "http://www.a.com"

  Scenario Outline: <method> with body created by spec
    When <method> "LoginRequest" "/index":
    """
    {
      "username": "admin"
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
        password: password#1
      }
    }]
    """
    Examples:
      | method |
      | POST   |
      | PUT    |
