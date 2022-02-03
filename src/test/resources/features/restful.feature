Feature: RESTful api steps

  Background:
    Given base url "http://www.a.com"

  Scenario: get with no params
    When GET "/index"
    Then "http://www.a.com" got a GET request on "/index"

  Scenario: get with header
    Given header by RESTful api:
    """
    {
      "key1": "value1",
      "key2": ["value2", "value3"]
    }
    """
#      "list": ["value1", "value2"]
    When GET "/index"
    Then got request:
    """
    : [{
      method: 'GET'
      path: '/index'
      headers: {
        key1: ['value1']
        key2: ['value2', 'value3']
      }
    }]
    """
    And "http://www.a.com" got a GET request on "/index"

#  TODO header for POST PUT DELETE