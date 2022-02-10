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

  Scenario: get response
    Given response 200 on GET "/index":
    """
    Hello world
    """
    When GET "/index"
    Then response should be:
    """
    : {
      code=200
      body.string='Hello world'
      raw.class.simpleName='Response'
    }
    """

#  TODO header for POST PUT DELETE